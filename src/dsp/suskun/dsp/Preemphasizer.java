package suskun.dsp;

import suskun.core.FloatData;
import suskun.core.FloatDataProcessor;

/**
 * This is a simple preemphasizer. Applies the formula of x[1] = x1[1] - x[0] * coefficient.
 * For x[0], x[0] = x[0] - x[0] * coefficient
 * This is an in-place operation, so that input data will be modified after processing.
 */
public class Preemphasizer implements FloatDataProcessor {

    float coefficient;

    public Preemphasizer(float coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public FloatData process(FloatData data) {
        float[] d = data.getData();
        if (coefficient == 0) {
            return data;
        }
        for (int i = 1; i < data.length() - 1; i++) {
            d[i] -= d[i - 1] * coefficient;
        }
        d[0] -= d[0] * coefficient;
        return data;
    }
}
