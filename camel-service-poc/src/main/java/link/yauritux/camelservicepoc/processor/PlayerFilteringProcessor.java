package link.yauritux.camelservicepoc.processor;

import link.yauritux.camelservicepoc.dto.ChessPlayer;
import link.yauritux.camelservicepoc.util.RandomAlpha;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
@Component
public class PlayerFilteringProcessor {

    public List<String> processMessage(ChessPlayer chessPlayer) {
        var randomPrefix = RandomAlpha.randomChar();
        return chessPlayer.getPlayers().stream()
                .filter(username -> username.charAt(0) == randomPrefix)
                .limit(1)
                .toList();
    }
}
