package suskun.core.collections;

public class FloatArrays {

    public static void multiplyToFirst(float[] first, float[] second) {
        checkArrays(first, second);
        for (int i = 0; i < first.length; i++) {
            first[i] = first[i] * second[i];
        }
    }

    public static void checkArrays(float[] first, float[] second) {
        if (first == null) {
            throw new IllegalArgumentException("First array is null.");
        }
        if (second == null) {
            throw new IllegalArgumentException("Second array is null.");
        }
        if (first.length != second.length) {
            throw new IllegalArgumentException("Array lengths are not equal. First is "
                    + first.length + " second is " + second.length);
        }
    }


}
