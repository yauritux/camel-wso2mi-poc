package link.yauritux.camelservicepoc.route;

import link.yauritux.camelservicepoc.processor.PlayerFilteringProcessor;
import link.yauritux.camelservicepoc.processor.PlayerSummaryProcessor;
import link.yauritux.camelservicepoc.dto.ChessPlayer;
import link.yauritux.camelservicepoc.dto.ChessPlayerStatistic;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
@Component
public class WebServiceRoute extends RouteBuilder {

    @Value("${chess.api.url}")
    private String chessApiUrl;

    @Override
    public void configure() throws Exception {
        // Define the retry mechanism for 429 responses
        onException(HttpOperationFailedException.class)
                .onWhen(exchange -> {
                    HttpOperationFailedException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    return exception != null && exception.getStatusCode() == 429; // Retry only for HTTP 429
                })
                .maximumRedeliveries(3) // Retry 3 times
                .redeliveryDelay(3000) // Wait 3 seconds between retries
                .backOffMultiplier(2) // Exponential backoff
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .logRetryAttempted(true)
                .handled(false);

        errorHandler(defaultErrorHandler()
                .maximumRedeliveries(3)
                .redeliveryDelay(3000)
                .backOffMultiplier(2)
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .onRedelivery(exchange -> {
                    Integer attemptCount = exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER, Integer.class);
                    log.warn("Retrying attempt {} for URL: {}", attemptCount, exchange.getIn().getHeader(Exchange.HTTP_URI));
                })
        );

        from("direct:fetchChessPlayers")
                .routeId("chess-api-route")
                .process(exchange -> {
                    long start = System.currentTimeMillis();
                    exchange.setProperty("startTime", start);
                })
                .to(chessApiUrl + "/titled/GM")
                .unmarshal().json(JsonLibrary.Jackson, ChessPlayer.class)
                .bean(PlayerFilteringProcessor.class)
                .split(body())
                .log(chessApiUrl + "/player/${body}/stats")
                .setProperty("username", simple("${body}"))
                .recipientList(simple(chessApiUrl + "/player/${body}/stats?httpMethod=GET"))
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
                .end()
                .setBody(simple("${body}"));

        from("direct:jdbcInsert")
                .routeId("jdbc-insert-route")
                .setHeader("data", body())
                .setBody(simple("INSERT INTO camel_archive_db(id, data, time_ms) values(uuid_generate_v4(), cast(:?data as json), :?time_ms)"))
                .to("jdbc:dataSource?useHeadersAsParameters=true");

        from("direct:sendToKafka")
                .routeId("kafka-producer-route")
                .to("kafka:chess_player");

        from("direct:errorHandler")
                .routeId("error-handler-route")
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String failedEndpoint = exchange.getProperty(Exchange.FAILURE_ENDPOINT, String.class);
                    Integer httpStatus = exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
                    String requestBody = exchange.getIn().getBody(String.class);

                    log.error("Error occurred:");
                    log.error(" - Exception: {}", exception != null ? exception.getMessage() : "Unknown");
                    log.error(" - HTTP Status: {}", httpStatus != null ? httpStatus : "N/A");
                    log.error(" - Failed Endpoint: {}", failedEndpoint);
                    log.error(" - Original Request Body: {}", requestBody);

                    exchange.getMessage().setBody(Map.of(
                            "status", "error",
                            "message", exception != null ? exception.getMessage() : "An unexpected error occurred",
                            "httpStatus", httpStatus != null ? httpStatus : "N/A",
                            "failedEndpoint", failedEndpoint
                    ));
                })
                .marshal().json()
                .log("Error response sent to the caller: ${body}")
                .to("log:error?level=ERROR");
    }
}
