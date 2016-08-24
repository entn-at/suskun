package suskun.dsp;

import suskun.core.FloatData;
import suskun.core.FloatDataProcessor;

import java.util.Random;

import static java.lang.Math.*;

/**
 * Adapted from Kaldi. But we use a cached version.
 */
public class Dither implements FloatDataProcessor {

    private static final int VALUE_AMOUNT = 1024;
    public float multiplier;
    // cached dither values.
    private float[] values;

    private Random random = new Random();

    public Dither(float multiplier) {
        this.multiplier = multiplier;

        values = new float[VALUE_AMOUNT];
        for (int i = 0; i < values.length; i++) {
            values[i] = (float) (sqrt(-2 * log(random.nextFloat())) * cos(2 * PI * random.nextFloat())) * multiplier;

        }
    }

    @Override
    public FloatData process(FloatData input) {
        int startIndex = random.nextInt(values.length);
        float[] data = input.getData();
        for (int i = 0; i < input.length(); i++) {
            data[i] = data[i] + values[startIndex % VALUE_AMOUNT];
            startIndex++;
        }
        return input;
    }

    public static void main(String[] args) {
        new Dither(1);
        
    }
}

