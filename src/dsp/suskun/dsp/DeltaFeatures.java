package suskun.dsp;

import suskun.core.FloatData;
import suskun.core.collections.FloatArrays;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapted from Kaldi.
 */
public class DeltaFeatures {

    private float[][] scales;
    public final int order;
    public final int size;
    public final boolean live;

    List<FloatData> current = new ArrayList<>();


    public DeltaFeatures(int order, boolean live) {
        this.order = order;
        this.live = live;

        // calculate scale values.
        scales = new float[order + 1][];
        scales[0] = new float[1];
        scales[0][0] = 1f;
        for (int i = 1; i <= order; i++) {
            float[] previousScales = scales[i - 1];

            int prevOffset = (previousScales.length - 1) / 2;
            int currentOffset = prevOffset + order;

            scales[i] = new float[previousScales.length + 2 * order];
            float[] currentScales = scales[i];

            float normalizer = 0;
            for (int j = -order; j <= order; j++) {
                normalizer += j * j;
                for (int k = -prevOffset; k <= prevOffset; k++) {
                    currentScales[j + k + currentOffset] += (j * 1.0f) * previousScales[k + prevOffset];
                }
            }
            FloatArrays.scaleInPlace(currentScales, 1f / normalizer);
        }
        this.size = (scales[order].length - 1) / 2;
    }


    List<FloatData> get(List<FloatData> input) {

        if (input.size() == 0) {
            return new ArrayList<>(0);
        }

        // This means first time usage.
        if (current.isEmpty()) {
            // we repeat the first one `past` times to generate initial frames nicely.
            for (int i = 0; i < size; i++) {
                current.add(input.get(0));
            }
        }

        // add all
        current.addAll(input);

        // if not live, add last frame `future` time.
        if (!live) {
            for (int i = 0; i < size; i++) {
                current.add(input.get(input.size() - 1));
            }
        }

        if (current.size() < size * 2 + 1) {
            return new ArrayList<>(0);
        }

        int inputVectorLength = current.get(0).length();

        List<FloatData> result = new ArrayList<>();

        for (int frameIndex = size; frameIndex < current.size() - size; frameIndex++) {

            float[] deltaFeatures = new float[inputVectorLength * (order + 1)];

            for (int i = 0; i <= order; i++) {
                float[] scaleArr = scales[i];
                int maxOffset = (scaleArr.length - 1) / 2;
                float[] frame = new float[inputVectorLength];
                for (int j = -maxOffset; j <= maxOffset; j++) {
                    int offsetFrame = frameIndex + j;
                    float scale = scaleArr[j + maxOffset];
                    if (scale != 0) {
                        FloatArrays.addToFirstScaled(frame, current.get(offsetFrame).getData(), scale);
                    }
                }
                System.arraycopy(frame, 0, deltaFeatures, i * inputVectorLength, inputVectorLength);
            }
            result.add(current.get(frameIndex).copyFor(deltaFeatures));
        }

        //Remove the processed frames from the current.
        current = new ArrayList<>(current.subList(current.size() - 2 * size, current.size()));

        return result;
    }


}
