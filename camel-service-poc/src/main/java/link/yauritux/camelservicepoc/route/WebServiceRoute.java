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
                .to(chessApiUrl + "/titled/GM")
                .unmarshal().json(JsonLibrary.Jackson, ChessPlayer.class)
                .bean(PlayerFilteringProcessor.class)
                .split(body())
                .log(chessApiUrl + "/player/${body}/stats")
                .setProperty("username", simple("${body}"))
                .recipientList(simple(chessApiUrl + "/player/${body}/stats?httpMethod=GET"))
                .unmarshal().json(JsonLibrary.Jackson, ChessPlayerStatistic.class)
                .bean(PlayerSummaryProcessor.class)
                .end()
                .setBody(simple("${body}"));
    }
}
