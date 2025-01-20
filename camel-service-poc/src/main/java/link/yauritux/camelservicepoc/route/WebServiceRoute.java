package link.yauritux.camelservicepoc.route;

import link.yauritux.camelservicepoc.dto.ChessPlayer;
import link.yauritux.camelservicepoc.processor.PlayerFilteringProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

        from("direct:fetchChessPlayers")
                .routeId("chess-api-route")
                .threads()
                .executorService(new ThreadPoolExecutor(
                        20, 50, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100)))
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
                .throttle(50)
                .to("direct:callChessPlayerStats")
                .setBody(simple("${body}"))
                .end();

//        from("direct:errorHandler")
//                .routeId("error-handler-route")
//                .process(exchange -> {
//                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
//                    String failedEndpoint = exchange.getProperty(Exchange.FAILURE_ENDPOINT, String.class);
//                    Integer httpStatus = exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
//                    String requestBody = exchange.getIn().getBody(String.class);
//
//                    log.error("Error occurred:");
//                    log.error(" - Exception: {}", exception != null ? exception.getMessage() : "Unknown");
//                    log.error(" - HTTP Status: {}", httpStatus != null ? httpStatus : "N/A");
//                    log.error(" - Failed Endpoint: {}", failedEndpoint);
//                    log.error(" - Original Request Body: {}", requestBody);
//
//                    exchange.getMessage().setBody(Map.of(
//                            "status", "error",
//                            "message", exception != null ? exception.getMessage() : "An unexpected error occurred",
//                            "httpStatus", httpStatus != null ? httpStatus : "N/A",
//                            "failedEndpoint", failedEndpoint
//                    ));
//                })
//                .marshal().json()
//                .log("Error response sent to the caller: ${body}")
//                .to("log:error?level=WARN")
//                .end();
    }
}
