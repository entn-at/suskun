package suskun.dsp;

import suskun.core.FloatData;
import suskun.core.FloatDataProcessor;
import suskun.core.collections.FloatArrays;

import java.util.Arrays;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Adapted from Kaldi.
 */
public class MelCepstrum implements FloatDataProcessor {

    /**
     * the number of filters in the filter bank.
     */
    public final int numberOfMelFilters;

    /**
     * size of the cepstrum.
     */
    public final int cepstrumSize;

    /**
     * matrix containing shifted cosine values.
     */
    private float[][] melCosine;

    public final float lifteringCoefficient;

    private float[] lifteringWeights;

    public MelCepstrum(int cepstrumSize, int numberOfMelFilters, int lifteringCoefficient) {
        this.cepstrumSize = cepstrumSize;
        this.numberOfMelFilters = numberOfMelFilters;
        this.melCosine = new float[cepstrumSize][numberOfMelFilters];

        this.lifteringCoefficient = lifteringCoefficient;
        if (lifteringCoefficient != 0) {
            this.lifteringWeights = new float[cepstrumSize];
            for (int i = 0; i < lifteringWeights.length; i++) {
                lifteringWeights[i] = 1 + lifteringCoefficient * 0.5f * (float) sin(i * PI / lifteringCoefficient);
            }
        }
        // compute mel cosine.
        Arrays.fill(melCosine[0], (float) Math.sqrt(1.0 / numberOfMelFilters));
        float normScale = (float) Math.sqrt(2.0 / numberOfMelFilters);
        for (int i = 1; i < cepstrumSize; i++) {
            float frequency = (float) (PI * i / numberOfMelFilters);
            for (int j = 0; j < numberOfMelFilters; j++) {
                melCosine[i][j] = (float) (normScale * cos(frequency * (j + 0.5)));
            }
        }
    }

    public FloatData process(FloatData input) throws IllegalArgumentException {

        float[] cepstrum = applyMelCosine(input.getData());
        if (lifteringCoefficient != 0) {
            applyLiftering(cepstrum);
        }
        return input.copyFor(cepstrum);
    }

    protected void applyLiftering(float[] values) {
        FloatArrays.multiplyToFirst(values, lifteringWeights);
    }

    private float[] applyMelCosine(float[] melSpectrum) {
        // create the cepstrum
        float[] cepstrum = new float[cepstrumSize];
        for (int i = 0; i < cepstrum.length; i++) {
            for (int j = 0; j < numberOfMelFilters; j++) {
                cepstrum[i] += melSpectrum[j] * melCosine[i][j];
            }
        }
        return cepstrum;
    }

}
