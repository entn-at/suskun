package suskun.dsp;

import suskun.core.FloatData;
import suskun.core.FloatDataProcessor;
import suskun.core.collections.FloatArrays;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

public class WindowFunction implements FloatDataProcessor {

    float[] multiplier;
    public final Function function;

    public WindowFunction(Function function, int windowSize) {

        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be a positive integer. But it is " + windowSize);
        }
        this.function = function;
        this.multiplier = new float[windowSize];
        for (int i = 0; i < multiplier.length; i++) {
            double v = 0;
            double cos = cos(2 * PI * i / (windowSize - 1));
            switch (function) {
                case HAMMING:
                    v = 0.54 - 0.46 * cos;
                    break;
                case HANNING:
                    v = 0.5 - 0.5 * cos;
                    break;
                case POVEY: // Kaldi
                    v = 0.5 - 0.5 * cos;
                    v = Math.pow(v, 0.85);
                    break;
            }
            multiplier[i] = (float) v;
        }
    }

    @Override
    public FloatData process(FloatData data) {
        FloatArrays.multiplyToFirst(data.getData(), multiplier);
        return data;
    }

    public enum Function {
        HAMMING,
        HANNING,
        POVEY
    }
}
