package suskun.asr.decoder;

public class Hypothesis {

    Hypothesis previous;
    int stateId;
    float score;

    public Hypothesis(Hypothesis previous, int stateId, float score) {
        this.previous = previous;
        this.stateId = stateId;
        this.score = score;
    }

    public float getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hypothesis that = (Hypothesis) o;

        return stateId == that.stateId;
    }

    @Override
    public int hashCode() {
        return stateId;
    }
}
