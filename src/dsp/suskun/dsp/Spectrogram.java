package suskun.dsp;

import suskun.core.FloatData;
import suskun.core.FloatDataProcessor;
import suskun.core.math.LogMath;

/**
 * Calculates energy spectrogram values. Adapted from Kaldi code.
 */
public class Spectrogram implements FloatDataProcessor {

    public final FastFourierTransform fft;
    public final boolean applyLog;

    public Spectrogram(FastFourierTransform fft) {
        this.fft = fft;
        this.applyLog = false;
    }

    public Spectrogram(FastFourierTransform fft, boolean applyLog) {
        this.fft = fft;
        this.applyLog = false;
    }

    @Override
    public FloatData process(FloatData input) {

        input = fft.process(input);

        float[] fftValues = input.getData();
        float[] energy = new float[fftValues.length / 2 + 1];

        float first = fftValues[0] * fftValues[0];
        float last = fftValues[1] * fftValues[1];

        for (int i = 1; i < energy.length - 1; i++) {
            float real = fftValues[i * 2], imaginary = fftValues[i * 2 + 1];
            energy[i] = real * real + imaginary * imaginary;
        }

        energy[0] = first;
        energy[energy.length - 1] = last;

        if (applyLog) {
            LogMath.LINEAR_TO_LOG_FLOAT.convertInPlace(energy);
        }

        return input.copyFor(energy);
    }
}
