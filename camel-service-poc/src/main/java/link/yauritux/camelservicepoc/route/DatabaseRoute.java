package link.yauritux.camelservicepoc.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
@Component
public class DatabaseRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:jdbcInsert")
                .routeId("jdbc-insert-route")
                .setHeader("data", body())
                .setBody(simple("INSERT INTO camel_write_db(id, data) values(uuid_generate_v4(), cast(:?data as json))"))
                .to("jdbc:dataSource?useHeadersAsParameters=true");
    }
}
