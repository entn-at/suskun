package suskun.core;

import java.util.Arrays;

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


}
