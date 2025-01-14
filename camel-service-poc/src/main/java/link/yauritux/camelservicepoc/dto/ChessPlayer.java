package link.yauritux.camelservicepoc.dto;

import java.util.List;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
public class ChessPlayer {

    private List<String> players;

    public ChessPlayer() {}

    public ChessPlayer(List<String> players) {
        this.players = players;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }
}
