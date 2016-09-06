package suskun.dsp;

import suskun.core.FloatData;

import java.util.ArrayList;
import java.util.List;

public class WindowFeatures {

    int past = 0;
    int future = 0;
    int paddedSize = -1;
    boolean live;

    List<FloatData> current = new ArrayList<>();

    WindowFeatures(Builder builder) {
        this.past = builder.past;
        this.future = builder.future;
        this.paddedSize = builder.paddedSize;
        this.live = builder.live;
    }

    public static Builder builder(int past, int future) {
        return new Builder(past, future);
    }

    public static class Builder {
        int past;
        int future;
        boolean live;
        int paddedSize = -1;

        public Builder(int past, int future) {
            this.past = past;
            this.future = future;
        }

        public Builder setLiveMode(boolean live) {
            this.live = live;
            return this;
        }

        public Builder paddedSize(int paddedSize) {
            this.paddedSize = paddedSize;
            return this;
        }

        public WindowFeatures build() {
            return new WindowFeatures(this);
        }
    }

    public List<FloatData> get(List<FloatData> input) {

        if (input.size() == 0) {
            return new ArrayList<>(0);
        }

        // This means first time usage.
        if (current.isEmpty()) {
            // we repeat the first one `past` times to generate initial frames nicely.
            for (int i = 0; i < past; i++) {
                current.add(input.get(0));
            }
        }

        // add all
        current.addAll(input);

        // if not live, add last frame `future` time.
        if (!live) {
            for (int i = 0; i < future; i++) {
                current.add(input.get(input.size() - 1));
            }
        }

        if (current.size() < past + future + 1) {
            return new ArrayList<>(0);
        }

        int inputVectorLength = input.get(0).length();
        int outputVectorLength = (past + future + 1) * inputVectorLength;

        // if set, use padded size.
        if (paddedSize > 0) {
            if (paddedSize < outputVectorLength) {
                throw new IllegalStateException(
                        String.format("Padded size %d is smaller than output vector size %d ",
                                paddedSize, outputVectorLength));
            } else {
                outputVectorLength = paddedSize;
            }
        }

        List<FloatData> features = new ArrayList<>();

        // Now we concatenate past+future+1 vectors and generate a large vector.
        for (int i = past; i < current.size() - future; i++) {

            float[] concat = new float[outputVectorLength];

            int c = 0;
            for (int j = i - past; j <= i + future; j++) {
                float[] data = current.get(j).getData();
                System.arraycopy(data, 0, concat, data.length * c, data.length);
                c++;
            }

            features.add(current.get(i).copyFor(concat));
        }

        current = new ArrayList<>(current.subList(current.size() - (past + future), current.size()));

        return features;
    }
}
