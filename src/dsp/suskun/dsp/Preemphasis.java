package suskun.dsp;

import suskun.core.FloatData;
import suskun.core.FloatDataProcessor;

import java.util.List;

/**
 * This applies preemphasis to input with the
 * formula x[1] = x1[1] - x[0] * coefficient from n-1..0
 * x[0] = x[0] - x[0] * coefficient for 0
 * Process is in-place, so that input data will be modified after processing.
 * This is a stateless preemphasis function.
 */
public class Preemphasis implements FloatDataProcessor {

    float coefficient;

    public Preemphasis(float coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public FloatData process(FloatData data) {
        float[] d = data.getData();
        if (coefficient == 0) {
            return data;
        }
        for (int i = data.length() - 1; i > 0; i--) {
            d[i] -= d[i - 1] * coefficient;
        }
        d[0] -= d[0] * coefficient;
        return data;
    }

    public void processAll(List<FloatData> input) {
        input.forEach(this::process);
    }

}
