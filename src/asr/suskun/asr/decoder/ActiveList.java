package suskun.asr.decoder;

import com.google.common.math.IntMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fast active list implementation. It uses a linear probing hash table like structure.
 */

class ActiveList {

    static final Logger logger = LoggerFactory.getLogger(ActiveList.class);

    public static float DEFAULT_LOAD_FACTOR = 0.7f;

    public static int DEFAULT_INITIAL_CAPACITY = 4096 * 2;

    public static int DEFAULT_CLUSTER_COUNT = 100;

    public static int DEFAULT_MIN_HYPOTHESIS_COUNT = 1000;

    private Hypothesis[] hypotheses;
    private int capacity = DEFAULT_INITIAL_CAPACITY;
    private float beamSize;
    private float loadFactor = DEFAULT_LOAD_FACTOR;

    private float min = Float.POSITIVE_INFINITY;
    private float max = Float.NEGATIVE_INFINITY;

    private int modulo;

    private int size;

    private int expandLimit;

    private int minHypothesisCount = DEFAULT_MIN_HYPOTHESIS_COUNT;

    private int clusterCount = DEFAULT_CLUSTER_COUNT;

    private Builder builder;

    public ActiveList(Builder builder) {
        this.beamSize = builder.beamSize;
        this.capacity = equalOrLargerPowerOfTwo(builder.initialCapacity);
        this.hypotheses = new Hypothesis[capacity];
        this.loadFactor = builder.loadFactor;
        this.expandLimit = (int) (loadFactor * capacity);
        this.minHypothesisCount = builder.minimumHypothesisCount;
        this.clusterCount = builder.clusterCount;
        this.modulo = capacity - 1;
        // save this builder for cloning.
        this.builder = builder;
    }

    private ActiveList copyForExpansion() {
        builder.initialCapacity = capacity * 2;
        return new ActiveList(builder);
    }

    private ActiveList newInstance() {
        return new ActiveList(builder);
    }

    public int getCapacity() {
        return capacity;
    }

    public float getBeamSize() {
        return beamSize;
    }

    public float getLoadFactor() {
        return loadFactor;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public int size() {
        return size;
    }

    public int getMinHypothesisCount() {
        return minHypothesisCount;
    }

    public int getClusterCount() {
        return clusterCount;
    }

    public static Builder builder(float beamSize) {
        return new Builder(beamSize);
    }

    public static class Builder {
        float beamSize;
        int initialCapacity = DEFAULT_INITIAL_CAPACITY;
        float loadFactor = DEFAULT_LOAD_FACTOR;
        int minimumHypothesisCount = DEFAULT_MIN_HYPOTHESIS_COUNT;
        int clusterCount = DEFAULT_CLUSTER_COUNT;

        public Builder(float beamSize) {
            this.beamSize = beamSize;
        }

        public Builder initialCapacity(int histogramSize) {
            this.initialCapacity = histogramSize;
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
                final Hypothesis h = expandedList.hypotheses[slot];
                if (h == null) {
                    expandedList.hypotheses[slot] = hyp;
                    break;
                }
                slot = nextProbe(slot, ++probeCount);
            }
        }
        this.hypotheses = expandedList.hypotheses;
        this.modulo = expandedList.modulo;
        this.capacity = expandedList.capacity;
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

        clusters.forEach(result::addAll);
        return result;
    }
}
