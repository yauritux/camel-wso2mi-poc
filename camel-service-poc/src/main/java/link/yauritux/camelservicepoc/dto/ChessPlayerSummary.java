package link.yauritux.camelservicepoc.dto;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
public class ChessPlayerSummary {

    private String username;
    private ChessPlayerStatistic gameStatistic;

    public ChessPlayerSummary() {}

    public ChessPlayerSummary(String username, ChessPlayerStatistic gameStatistic) {
        this.username = username;
        this.gameStatistic = gameStatistic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ChessPlayerStatistic getGameStatistic() {
        return gameStatistic;
    }

    public void setGameStatistic(ChessPlayerStatistic gameStatistic) {
        this.gameStatistic = gameStatistic;
    }
}
