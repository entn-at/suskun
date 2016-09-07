package suskun.asr.decoder;

import com.google.common.math.IntMath;

import java.math.RoundingMode;

/**
 * A fast active list implementation. It uses a linear probing hash table like structure. However, it does not
 * make remove operations.
 */

public class ActiveList {

    static float DEFAULT_LOAD_FACTOR = 0.7f;
    static int DEFAULT_BIN_SIZE = 4096*2;

    Hypothesis[] hypotheses;
    int histogramSize = DEFAULT_BIN_SIZE;
    float beamSize;
    float loadFactor = DEFAULT_LOAD_FACTOR;
    float min = Float.POSITIVE_INFINITY;
    float max = Float.NEGATIVE_INFINITY;
    int modulo;
    int size;
    int threshold;

    public ActiveList(float beamSize) {
        this.beamSize = beamSize;
        this.hypotheses = new Hypothesis[histogramSize];
        this.threshold = (int) (loadFactor * histogramSize);
        this.modulo = histogramSize - 1;
    }

    public ActiveList(int histogramSize, float beamSize) {
        this.beamSize = beamSize;
        this.histogramSize = equalOrLargerPowerOfTwo(histogramSize);
        this.hypotheses = new Hypothesis[histogramSize];
        this.threshold = (int) (loadFactor * histogramSize);
        this.modulo = histogramSize - 1;
    }

    int equalOrLargerPowerOfTwo(int i) {
        return IntMath.isPowerOfTwo(i) ? i : IntMath.pow(2, IntMath.log2(i, RoundingMode.UP));
    }

    // Extra hashing may be necessary. This is similar to Java Map's extra hashing.
    private int firstProbe(int hashCode) {
        return (hashCode ^ ((hashCode << 5) + (hashCode >>> 2))) & modulo;
    }

    private int nextProbe(int previous, int count) {
        return (previous + count) & modulo;
    }

    /**
     * Finds either an empty slot location in Hypotheses array or the location of an equivalent Hypothesis.
     * If an empty slot is found, it returns -(slot index)-1, if an equivalent Hypotheses is found, returns
     * equal hypothesis's slot index.
     */
    private int locate(Hypothesis hyp) {
        int count = 0;
        int slot = firstProbe(hyp.hashCode());
        while (true) {
            final Hypothesis h = hypotheses[slot];
            if (h == null) {
                return (-slot - 1);
            }
            if (h.equals(hyp)) {
                return slot;
            }
            slot = nextProbe(slot, ++count);
        }
    }

    public void add(Hypothesis hypothesis) {

        int slot = locate(hypothesis);

        if (slot < 0) {
            slot = -slot - 1;
            hypotheses[slot] = hypothesis;
            size++;
        } else {
            // Viterbi merge.
            if (hypotheses[slot].score < hypothesis.score) {
                hypotheses[slot] = hypothesis;
            }
        }
        updateMinMax(hypothesis.score);
        if (size == threshold) {
            expand();
        }
    }

    private void updateMinMax(float score) {
        if (min > score) {
            min = score;
        }
        if (max < score) {
            max = score;
        }
    }

    private void expand() {
        ActiveList expandedList = new ActiveList(histogramSize * 2, beamSize);
        // put hypotheses to new list.
        for (int i = 0; i < hypotheses.length; i++) {
            Hypothesis hyp = hypotheses[i];
            if (hyp == null) {
                continue;
            }
            int probeCount = 0;
            int slot = firstProbe(hyp.hashCode());
            while (true) {
                final Hypothesis h = hypotheses[slot];
                if (h == null) {
                    expandedList.hypotheses[slot] = hyp;
                    break;
                }
                slot = nextProbe(slot, ++probeCount);
            }
        }
        this.hypotheses = expandedList.hypotheses;
        this.modulo = expandedList.modulo;
        this.histogramSize = expandedList.histogramSize;
        this.threshold = expandedList.threshold;
    }


}
