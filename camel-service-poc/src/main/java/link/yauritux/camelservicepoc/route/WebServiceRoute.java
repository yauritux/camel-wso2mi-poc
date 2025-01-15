package link.yauritux.camelservicepoc.route;

import link.yauritux.camelservicepoc.bean.PlayerFilteringProcessor;
import link.yauritux.camelservicepoc.bean.PlayerSummaryProcessor;
import link.yauritux.camelservicepoc.dto.ChessPlayer;
import link.yauritux.camelservicepoc.dto.ChessPlayerStatistic;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    }
}
