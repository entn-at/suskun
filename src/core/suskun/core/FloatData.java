package suskun.core;

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

    public float[] getData() {
        return data;
    }

    public float[] getCopyOfData() {
        return data.clone();
    }

    public FloatData copyFor(float[] otherData) {
        return new FloatData(this.id, otherData);
    }

    public int length() {
        return data.length;
    }


}
