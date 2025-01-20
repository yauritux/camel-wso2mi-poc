package link.yauritux.camelservicepoc.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
@Component
public class KafkaRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:sendToKafka")
                .routeId("kafka-producer-route")
                .to("kafka:chess_player");
    }
}
