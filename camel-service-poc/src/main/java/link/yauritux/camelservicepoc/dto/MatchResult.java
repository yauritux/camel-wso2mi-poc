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
public class MatchResult {

    private int win;
    private int loss;
    private int draw;
    @JsonProperty("time_per_move")
    private int timePerMove;
    @JsonProperty("timeout_percent")
    private int timeoutPercent;

    public MatchResult() {}

    public MatchResult(int win, int loss, int draw) {
        this.win = win;
        this.loss = loss;
        this.draw = draw;
    }

    public int getWin() {
        return win;
    }

    public void setWin(int win) {
        this.win = win;
    }

    public int getLoss() {
        return loss;
    }

    public void setLoss(int loss) {
        this.loss = loss;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }
}
