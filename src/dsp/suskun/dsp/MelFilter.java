package suskun.dsp;

import suskun.core.FloatData;
import suskun.core.FloatDataProcessor;
import suskun.core.math.LogMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MelFilter implements FloatDataProcessor {

    public int spectrogramSize;
    float minimumFrequency;
    float maximumFrequency;
    int filterCount;
    float nyquistFrequency;

    List<Filter> filters = new ArrayList<>();

    public MelFilter(int samplingRate,
                     int spectrogramSize,
                     float minimumFrequency,
                     float maximumFrequency,
                     int filterCount) {
        this.nyquistFrequency = samplingRate / 2f;
        this.spectrogramSize = spectrogramSize;
        this.minimumFrequency = minimumFrequency;
        this.maximumFrequency = maximumFrequency;
        this.filterCount = filterCount;
        generateFilters();
    }

    /**
     * Generates triangular Mel band filters.
     */
    private void generateFilters() {

        float minMel = linearToMel(minimumFrequency);
        float maxMel = linearToMel(maximumFrequency);
        // Same length bins in mel domain produces increasing bin lengths in mel domain
        float interval = (maxMel - minMel) / (filterCount + 1);

        // defines the frequency equivalent of a discrete sample
        float fftBinWidth = nyquistFrequency / spectrogramSize;

        for (int bin = 0; bin < filterCount; bin++) {

            float leftMel = minMel + bin * interval;
            float centerMel = minMel + (bin + 1) * interval;
            float rightMel = minMel + (bin + 2) * interval;

            float[] weights = new float[spectrogramSize];
            int start = -1, end = 0;
            for (int i = 0; i < spectrogramSize; i++) {
                float freq = i * fftBinWidth;
                float melFreq = linearToMel(freq);
                if (melFreq <= leftMel || melFreq >= rightMel) {
                    continue;
                }
                // weight is determined by the left or right slope of the triangle
                float weight = (melFreq <= centerMel) ?
                        (melFreq - leftMel) / (centerMel - leftMel) :
                        (rightMel - melFreq) / (rightMel - centerMel);
                weights[i] = weight;
                if (start == -1)
                    start = i;
                end = i + 1;

            }
            float[] filterWeights = new float[end - start];
            System.arraycopy(weights, start, filterWeights, 0, filterWeights.length);
            filters.add(new Filter(start, end, filterWeights));
        }
    }

    @Override
    public FloatData process(FloatData data) {
        float[] binEnergies = new float[filterCount];
        int i = 0;
        for (Filter filter : filters) {
            binEnergies[i++] = filter.apply(data.getData());
        }
        LogMath.LINEAR_TO_LOG_FLOAT.convertInPlace(binEnergies);
        return data.copyFor(binEnergies);
    }

    private static class Filter {
        int sampleStart; // inclusive
        int sampleEnd; // exclusive
        float[] weights;

        private Filter(int sampleStart, int sampleEnd, float[] weights) {
            this.sampleStart = sampleStart;
            this.sampleEnd = sampleEnd;
            this.weights = weights;
        }

        public float apply(float[] samples) {
            float result = 0;
            for (int j = sampleStart; j < sampleEnd; j++) {
                result += (samples[j] * weights[j - sampleStart]);
            }
            return result;
        }

        @Override
        public String toString() {
            return sampleStart + "-" + sampleEnd + " : " + Arrays.toString(weights);
        }
    }

    /**
     * Converts to Linear frequency value to Mel frequency value.
     */
    public static float linearToMel(float linearFreq) {
        return (float) (2595.0 * Math.log10(1.0 + linearFreq / 700.0));
    }

    /**
     * Converts Mel frequency to linear frequency value.
     */
    public static float melToLinear(float melFreq) {
        return (float) (700.0 * (Math.pow(10.0, (melFreq / 2595.0)) - 1.0));
    }


}
