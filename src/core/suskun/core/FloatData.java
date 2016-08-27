package suskun.core;

import java.util.Arrays;
import java.util.Locale;

/**
 * A basic wrapper for a float array and integer id value.
 */
public class FloatData {

    public final int id;
    private float[] data;

    public FloatData(int id, float[] data) {
        this.id = id;
        this.data = data;
    }

    public FloatData(float[] data) {
        this.id = 0;
        this.data = data;
    }

    /**
     * @return data itself.
     */
    public float[] getData() {
        return data;
    }

    /**
     * @return a copy of the data.
     */
    public float[] getCopyOfContent() {
        return data.clone();
    }

    /**
     * Returns a new FloatData object with the given data
     *
     * @param data
     * @return
     */
    public FloatData copyFor(float[] data) {
        return new FloatData(this.id, data);
    }

    public FloatData copy() {
        return new FloatData(this.id, data.clone());
    }

    public void resizeInPlace(int size) {
        this.data = Arrays.copyOf(this.data, size);
    }


    public FloatData append(float[] toAppend) {
        float[] newContent = Arrays.copyOf(data, data.length + toAppend.length);
        System.arraycopy(toAppend, 0, newContent, data.length, toAppend.length);
        return new FloatData(id, newContent);
    }

    public void appendInPlace(float[] toAppend) {
        float[] newContent = Arrays.copyOf(data, data.length + toAppend.length);
        System.arraycopy(toAppend, 0, newContent, data.length, toAppend.length);
        this.data = newContent;
    }

    public FloatData prepend(float[] toPrepend) {
        float[] newContent = Arrays.copyOf(toPrepend, toPrepend.length + data.length);
        System.arraycopy(data, 0, newContent, toPrepend.length, data.length);
        return new FloatData(id, newContent);
    }

    public void prependInPlace(float[] toPrepend) {
        float[] newContent = Arrays.copyOf(toPrepend, toPrepend.length + data.length);
        System.arraycopy(data, 0, newContent, toPrepend.length, data.length);
        this.data = newContent;
    }

    public int length() {
        return data.length;
    }

    public boolean isEmpty() {
        return data.length == 0;
    }

    public static FloatData empty() {
        return new FloatData(0, new float[0]);
    }

    public void replaceData(float[] data) {
        this.data = data;
    }

    public String toString() {
        return id + " " + format(10, 5, " ", data);
    }

    public String toString(int amount) {
        return id + " " + format(10, 5, " ", Arrays.copyOf(data, amount));
    }


    /**
     * Formats a float array as string using English Locale.
     */
    public static String format(int rightPad, int fractionDigits, String delimiter, float... input) {
        StringBuilder sb = new StringBuilder();
        String formatStr = "%." + fractionDigits + "f";
        int i = 0;
        for (float v : input) {
            String num = String.format(formatStr, v);
            sb.append(String.format(Locale.ENGLISH, "%-" + rightPad + "s", num));
            if (i++ < input.length - 1) sb.append(delimiter);
        }
        return sb.toString().trim();
    }

    public static float[] alignTo(float[] input, int alignment) {
        int dimension = input.length;

        int padded = alignedSize(dimension, alignment);
        if (padded == dimension) {
            return input;
        }
        return Arrays.copyOf(input, padded);
    }

    public static int alignedSize(int size, int alignment) {
        if (size % alignment == 0) {
            return size;
        }
        return size + alignment - (size % alignment);
    }


}
