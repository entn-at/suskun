package suskun.core;

public class StringPair {
    public final String first;
    public final String second;

    public StringPair(String first, String second) {
        this.first = first;
        this.second = second;
    }

    public static StringPair fromString(String str) {
        int index = str.indexOf(" ");
        if (index == -1) {
            throw new IllegalArgumentException("Cannot extract two string from : [" + str + "]");
        }
        String first = str.substring(0, index).trim();
        String second = str.substring(index).trim();
        if (first.length() == 0 || second.length() == 0) {
            throw new IllegalArgumentException("Cannot extract two string from : [" + str + "]");
        }
        return new StringPair(first, second);
    }

}
