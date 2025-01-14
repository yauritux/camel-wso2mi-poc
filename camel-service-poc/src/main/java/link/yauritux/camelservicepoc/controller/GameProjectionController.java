package link.yauritux.camelservicepoc.controller;

import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/games")
public class GameProjectionController {

    private final ProducerTemplate producer;

    public GameProjectionController(ProducerTemplate producer) {
        this.producer = producer;
    }

    @GetMapping
    public ResponseEntity<String> getGameById() {
        var response = producer.requestBody("direct:fetchChessPlayers", null, String.class);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
