package suskun.dsp;


import suskun.core.FloatData;
import suskun.core.FloatDataProcessor;

import java.util.Arrays;

/**
 * Modified and converted to Java from LomontFFT code (www.lomont.org).
 * <p>
 * <p/>
 * Original LomontFFT code's copyright notice is below:
 * <p/>
 * Copyright Chris Lomont 2010-2012.
 * This code and any ports are free for all to use for any reason as long as this header is left in place.
 * Version 1.1, Sept 2011
 */

public class FastFourierTransform implements FloatDataProcessor {

    public final int size;
    /**
     * Pre-computed sine/cosine tables for speed
     */
    private final float[] cosTable;
    private final float[] sinTable;

    public FastFourierTransform(int size) {
        if ((size & (size - 1)) != 0)
            throw new IllegalArgumentException("data length " + size + " in FFT is not a power of 2");
        this.size = size;
        cosTable = new float[size];
        sinTable = new float[size];
        initialize();
    }

    /**
     * Result contains interleaved FFT transform results. First value is the real value of the first complex output
     * real[0] real[last] imaginary[] real[1] imaginary[1] ... real[size]
     *
     * @param input input FloatData
     * @return fft of the input.
     */
    @Override
    public FloatData process(FloatData input) {
        if (input.length() > size) throw new IllegalArgumentException(
                "input size=" + input.length() + "is larger than FFT size=" + size);
        return input.copyFor(realFft(input.getContent()));
    }


    /**
     * Compute the forward Fourier Transform of data, with data containing complex valued data as alternating real and
     * imaginary parts. The length must be a power of 2. This method caches values and should be slightly faster on
     * than the FFT method for repeated uses. It is also slightly more accurate. Data is transformed in place.
     *
     * @param data The complex data stored as alternating real and imaginary parts
     */
    public void tableFFT(float[] data) {
        int n = size / 2;    // n is the number of samples
        reverse(data, n); // bit index data reversal
        int mmax = 1;
        int tptr = 0;
        while (n > mmax) {
            int istep = 2 * mmax;
            for (int m = 0; m < istep; m += 2) {
                float wr = cosTable[tptr];
                float wi = sinTable[tptr++];
                for (int k = m; k < 2 * n; k += 2 * istep) {
                    int j = k + istep;
                    float tempr = wr * data[j] - wi * data[j + 1];
                    float tempi = wi * data[j] + wr * data[j + 1];
                    data[j] = data[k] - tempr;
                    data[j + 1] = data[k + 1] - tempi;
                    data[k] = data[k] + tempr;
                    data[k + 1] = data[k + 1] + tempi;
                }
            }
            mmax = istep;
        }
    }

    /**
     * Compute the forward Fourier Transform of data, with data containing real valued data only.
     * It contains interleaved complex values.
     *
     * @param input The complex data stored as alternating real and imaginary parts
     * @throws IllegalArgumentException if data size is larger than FFT size
     */
    public float[] realFft(float[] input) {

        if (input.length > size) {
            throw new IllegalArgumentException("input data length " + input.length + " is larger than FFT size " + size);
        }
        // make a copy and apply padding if necessary
        float data[] = Arrays.copyOf(input, size);

        tableFFT(data);

        float theta = (float) (2 * Math.PI / size);
        float wpr = (float) Math.cos(theta);
        float wpi = (float) Math.sin(theta);
        float wjr = wpr;
        float wji = wpi;

        for (int j = 1; j <= size / 4; ++j) {
            int k = size / 2 - j;
            float tkr = data[2 * k];    // real and imaginary parts of t_k  = t_(n/2 - j)
            float tki = data[2 * k + 1];
            float tjr = data[2 * j];    // real and imaginary parts of t_j
            float tji = data[2 * j + 1];

            float a = (tjr - tkr) * wji;
            float b = (tji + tki) * wjr;
            float c = (tjr - tkr) * wjr;
            float d = (tji + tki) * wji;
            float e = (tjr + tkr);
            float f = (tji - tki);

            // compute entry y[j]
            data[2 * j] = 0.5f * (e + (a + b));
            data[2 * j + 1] = 0.5f * (f + (d - c));

            // compute entry y[k]
            data[2 * k] = 0.5f * (e - (b + a));
            data[2 * k + 1] = 0.5f * ((d - c) - f);

            float temp = wjr;
            wjr = wjr * wpr - wji * wpi;
            wji = temp * wpi + wji * wpr;
        }

        float temp = data[0];
        data[0] += data[1];
        data[1] = temp - data[1];
        return data;
    }

    /**
     * fills sin and cos tables
     */
    private void initialize() {
        // forward pass
        int mmax = 1, pos = 0;
        while (size > mmax) {
            int istep = 2 * mmax;
            float theta = (float) (Math.PI / mmax);
            float wr = 1, wi = 0;
            float wpi = (float) Math.sin(theta);
            // compute in a slightly slower yet more accurate manner
            float wpr = (float) Math.sin(theta / 2);
            wpr = -2 * wpr * wpr;
            for (int m = 0; m < istep; m += 2) {
                cosTable[pos] = wr;
                sinTable[pos++] = wi;
                float t = wr;
                wr = wr * wpr - wi * wpi + wr;
                wi = wi * wpr + t * wpi + wi;
            }
            mmax = istep;
        }
    }

    /**
     * Swap data indices whenever index i has binary digits reversed from index j, where data is two floats per index.
     *
     * @param data data array
     * @param n    n
     */
    private void reverse(float[] data, int n) {
        // bit reverse the indices. This is exercise 5 in section
        // 7.2.1.1 of Knuth's TAOCP the idea is a binary counter
        // in k and one with bits reversed in j
        int j = 0, k = 0; // Knuth R1: initialize
        int top = n / 2;  // this is Knuth's 2^(n-1)
        while (true) {
            // Knuth R2: swap - swap j+1 and k+2^(n-1), 2 entries each
            float t = data[j + 2];
            data[j + 2] = data[k + n];
            data[k + n] = t;
            t = data[j + 3];
            data[j + 3] = data[k + n + 1];
            data[k + n + 1] = t;
            if (j > k) { // swap two more
                // j and k
                t = data[j];
                data[j] = data[k];
                data[k] = t;
                t = data[j + 1];
                data[j + 1] = data[k + 1];
                data[k + 1] = t;
                // j + top + 1 and k+top + 1
                t = data[j + n + 2];
                data[j + n + 2] = data[k + n + 2];
                data[k + n + 2] = t;
                t = data[j + n + 3];
                data[j + n + 3] = data[k + n + 3];
                data[k + n + 3] = t;
            }
            // Knuth R3: advance k
            k += 4;
            if (k >= n)
                break;
            // Knuth R4: advance j
            int h = top;
            while (j >= h) {
                j -= h;
                h /= 2;
            }
            j += h;
        } // bit reverse loop
    }
}

