package suskun.asr.decoder;

import com.google.common.math.IntMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fast active list implementation. It uses a linear probing hash table like structure. However, it does not
 * make remove operations.
 */

public class ActiveList {

    static final Logger logger = LoggerFactory.getLogger(ActiveList.class);

    static float DEFAULT_LOAD_FACTOR = 0.7f;

    static int DEFAULT_BIN_SIZE = 4096 * 2;

    static int DEFAULT_CLUSTER_SIZE = 100;

    static int DEFAULT_MIN_HYPOTHESIS_COUNT = 1000;

    Hypothesis[] hypotheses;
    int histogramSize = DEFAULT_BIN_SIZE;
    float beamSize;
    float loadFactor = DEFAULT_LOAD_FACTOR;

    float min = Float.POSITIVE_INFINITY;
    float max = Float.NEGATIVE_INFINITY;

    int modulo;

    int size;

    int expandLimit;

    int minHypothesisCount = DEFAULT_MIN_HYPOTHESIS_COUNT;

    int clusterCount = DEFAULT_CLUSTER_SIZE;

    private Builder builder;

    public ActiveList(Builder builder) {
        this.beamSize = builder.beamSize;
        this.histogramSize = equalOrLargerPowerOfTwo(builder.histogramSize);
        this.hypotheses = new Hypothesis[histogramSize];
        this.loadFactor = builder.loadFactor;
        this.expandLimit = (int) (loadFactor * histogramSize);
        this.minHypothesisCount = builder.minimumHypothesisCount;
        this.clusterCount = builder.clusterCount;
        this.modulo = histogramSize - 1;
        // save this builder for cloning.
        this.builder = builder;
    }

    private ActiveList copyForExpansion() {
        builder.histogramSize = builder.histogramSize * 2;
        return new ActiveList(builder);
    }

    public static class Builder {
        int beamSize;
        int histogramSize;
        float loadFactor;
        int minimumHypothesisCount;
        int clusterCount;

        public Builder(int beamSize) {
            this.beamSize = beamSize;
        }

        public Builder histogramSize(int histogramSize) {
            this.histogramSize = histogramSize;
            return this;
        }

        public Builder loadFactor(float loadFactor) {
            if (loadFactor < 0.1 || loadFactor > 0.9) {
                throw new IllegalArgumentException("Load factor must be between 0.1 and 0.9. But it is " + loadFactor);
            }
            this.loadFactor = loadFactor;
            return this;
        }

        public Builder minimumHypothesisCount(int minimumHypothesisCount) {
            this.minimumHypothesisCount = minimumHypothesisCount;
            return this;
        }

        public Builder clusterCount(int clusterCount) {
            this.clusterCount = clusterCount;
            return this;
        }

        public ActiveList build() {
            return new ActiveList(this);
        }

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

        // we skip the weak hypotheses if there are already some hypotheses around.
        if (size > minHypothesisCount && max - hypothesis.score > beamSize) {
            return;
        }

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
        if (size == expandLimit) {
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
        ActiveList expandedList = copyForExpansion();
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
        this.expandLimit = expandedList.expandLimit;
    }

    public List<Hypothesis> getAllHypotheses() {
        List<Hypothesis> result = new ArrayList<>(size + 1);
        for (int i = 0; i < hypotheses.length; i++) {
            Hypothesis hypothesis = hypotheses[i];
            if (hypothesis != null) {
                result.add(hypothesis);
            }
        }
        return result;
    }

    public List<List<Hypothesis>> cluster() {
        int averageClusterSize = size / clusterCount;

        List<List<Hypothesis>> clusters = new ArrayList<>(clusterCount + 2);
        for (int i = 0; i < clusterCount + 2; i++) {
            clusters.add(new ArrayList<>(averageClusterSize));
        }

        float interval = (max - min) / clusterCount;

        for (int i = 0; i < hypotheses.length; i++) {
            Hypothesis hyp = hypotheses[i];
            if (hyp == null) {
                continue;
            }
            int index = (int) ((max - hyp.score) / interval);
            clusters.get(index).add(hyp);
        }
        return clusters;
    }


    public List<Hypothesis> getActiveHypotheses() {
        if (size < minHypothesisCount) {
            List<Hypothesis> result = getAllHypotheses();
            Collections.sort(result);
            return result;
        }

        List<Hypothesis> result = new ArrayList<>(size);
        List<List<Hypothesis>> clusters = cluster();

        for (List<Hypothesis> cluster : clusters) {
            result.addAll(cluster);
        }
        return result;
    }
}
