package link.yauritux.camelservicepoc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChessPlayerStatistic {

    @JsonProperty("chess_daily")
    private ChessGroup chessDaily;
    @JsonProperty("chess_rapid")
    private ChessGroup chessRapid;
    @JsonProperty("chess_bullet")
    private ChessGroup chessBullet;
    @JsonProperty("chess_blitz")
    private ChessGroup chessBlitz;
    @JsonProperty("puzzle_rush")
    private ChessGroup puzzleRush;

    public ChessPlayerStatistic() {}

    public ChessPlayerStatistic(ChessGroup chessRapid, ChessGroup chessBullet, ChessGroup chessBlitz, ChessGroup puzzleRush) {
        this.chessRapid = chessRapid;
        this.chessBullet = chessBullet;
        this.chessBlitz = chessBlitz;
        this.puzzleRush = puzzleRush;
    }

    public ChessGroup getChessRapid() {
        return chessRapid;
    }

    public void setChessRapid(ChessGroup chessRapid) {
        this.chessRapid = chessRapid;
    }

    public ChessGroup getChessBullet() {
        return chessBullet;
    }

    public void setChessBullet(ChessGroup chessBullet) {
        this.chessBullet = chessBullet;
    }

    public ChessGroup getChessBlitz() {
        return chessBlitz;
    }

    public void setChessBlitz(ChessGroup chessBlitz) {
        this.chessBlitz = chessBlitz;
    }

    public ChessGroup getPuzzleRush() {
        return puzzleRush;
    }

    public void setPuzzleRush(ChessGroup puzzleRush) {
        this.puzzleRush = puzzleRush;
    }
}
