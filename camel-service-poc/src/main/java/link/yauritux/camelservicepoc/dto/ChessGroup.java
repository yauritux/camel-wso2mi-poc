package link.yauritux.camelservicepoc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Yauri Attamimi
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChessGroup {

    private RatingHistory last;
    private RatingHistory best;
    private MatchResult record;

    public ChessGroup() {}

    public ChessGroup(RatingHistory last, RatingHistory best, MatchResult record) {
        this.last = last;
        this.best = best;
        this.record = record;
    }

    public RatingHistory getLast() {
        return last;
    }

    public void setLast(RatingHistory last) {
        this.last = last;
    }

    public RatingHistory getBest() {
        return best;
    }

    public void setBest(RatingHistory best) {
        this.best = best;
    }

    public MatchResult getRecord() {
        return record;
    }

    public void setRecord(MatchResult record) {
        this.record = record;
    }
}
