package link.yauritux.camelservicepoc.route;

import link.yauritux.camelservicepoc.dto.ChessPlayerStatistic;
import link.yauritux.camelservicepoc.processor.PlayerSummaryProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
@Component
public class PlayerStatsApiRoute extends RouteBuilder {

    @Value("${chess.api.url}")
    private String chessApiUrl;

    @Override
    public void configure() throws Exception {
        // Define the retry mechanism for 429 responses
        onException(HttpOperationFailedException.class)
                .onWhen(exchange -> {
                    HttpOperationFailedException exception = exchange.getProperty(
                            Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    return exception != null && exception.getStatusCode() == 429; // Retry only for HTTP 429
                })
                .maximumRedeliveries(1) // Retry 1 time
                .redeliveryDelay(1000) // Wait 1 second between retries
                .backOffMultiplier(1) // Exponential backoff
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .logRetryAttempted(true)
                .handled(true);

        errorHandler(defaultErrorHandler()
                .maximumRedeliveries(1)
                .redeliveryDelay(1000)
                .backOffMultiplier(1)
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .onRedelivery(exchange -> {
                    Integer attemptCount = exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER, Integer.class);
                    log.warn("Retrying attempt {} for URL with 429 response", attemptCount);
                })
        );

        from("direct:callChessPlayerStats")
                .routeId("chess-player-stats-route")
                .threads()
//                .executorService(Executors.newVirtualThreadPerTaskExecutor())
                .executorService(new ThreadPoolExecutor(
                        50,
                        // Max pool size to handle 500 concurrent tasks
                        250,
                        60L, TimeUnit.SECONDS,
                        // Small queue to avoid excessive queuing of requests, keep queuing minimal to prioritize thread execution.
                        new LinkedBlockingQueue<Runnable>(50),
                        // Backpressure when overloaded
                        new ThreadPoolExecutor.CallerRunsPolicy()
                ))
                .process(exchange -> {
                    String username = exchange.getProperty("username", String.class);
                    exchange.getIn().setHeader("username", username);
                })
                .toD(chessApiUrl + "/player/${header.username}/stats?httpMethod=GET&useAsync=True")
//                .recipientList(simple(chessApiUrl + "/player/${header.username}/stats?httpMethod=GET"))
                .unmarshal().json(JsonLibrary.Jackson, ChessPlayerStatistic.class)
                .bean(PlayerSummaryProcessor.class)
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class)
                .process(exchange -> {
                    long stepStartTime = exchange.getProperty("startTime", Long.class);
                    long end = System.currentTimeMillis();
                    long timeMs = end - stepStartTime;
                    exchange.setProperty("time_ms", timeMs);
                    exchange.getMessage().setHeader("time_ms", timeMs);
                })
                .multicast().parallelProcessing()
                .to("direct:jdbcInsert", "direct:sendToKafka")
                .setBody(simple("${body}"))
                .end();
    }
}
