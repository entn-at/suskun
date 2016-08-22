package suskun.core;

import java.util.Arrays;

/**
 * A basic wrapper for a float array and integer id value.
 */
public class FloatData {

    public final int id;
    private float[] content;

    public FloatData(int id, float[] content) {
        this.id = id;
        this.content = content;
    }

    public FloatData(float[] content) {
        this.id = 0;
        this.content = content;
    }

    /**
     * @return content itself.
     */
    public float[] getContent() {
        return content;
    }

    /**
     * @return a copy of the content.
     */
    public float[] getCopyOfContent() {
        return content.clone();
    }

    /**
     * Returns a new FloatData object with the given content
     *
     * @param data
     * @return
     */
    public FloatData copyFor(float[] data) {
        return new FloatData(this.id, data);
    }

    public FloatData append(float[] toAppend) {
        float[] newContent = Arrays.copyOf(content, content.length + toAppend.length);
        System.arraycopy(toAppend, 0, newContent, content.length, toAppend.length);
        return new FloatData(id, newContent);
    }

    public void appendInPlace(float[] toAppend) {
        float[] newContent = Arrays.copyOf(content, content.length + toAppend.length);
        System.arraycopy(toAppend, 0, newContent, content.length, toAppend.length);
        this.content = newContent;
    }

    public FloatData prepend(float[] toPrepend) {
        float[] newContent = Arrays.copyOf(toPrepend, toPrepend.length + content.length);
        System.arraycopy(content, 0, newContent, toPrepend.length, content.length);
        return new FloatData(id, newContent);
    }

    public void prependInPlace(float[] toPrepend) {
        float[] newContent = Arrays.copyOf(toPrepend, toPrepend.length + content.length);
        System.arraycopy(content, 0, newContent, toPrepend.length, content.length);
        this.content = newContent;
    }

    public int length() {
        return content.length;
    }


}
