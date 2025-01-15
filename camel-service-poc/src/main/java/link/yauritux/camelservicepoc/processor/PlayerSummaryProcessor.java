package link.yauritux.camelservicepoc.processor;

import link.yauritux.camelservicepoc.dto.ChessPlayerStatistic;
import link.yauritux.camelservicepoc.dto.ChessPlayerSummary;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
@Component
public class PlayerSummaryProcessor {

    public ChessPlayerSummary processMessage(Exchange exchange) {
        return new ChessPlayerSummary(
                exchange.getProperty("username", String.class),
                exchange.getIn().getBody(ChessPlayerStatistic.class)
        );
    }
}
