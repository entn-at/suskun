package suskun.asr.acoustic;

public class Phone {
    public final String id;
    public final int index;
    public final boolean silence;
    public final boolean filler;
    public final boolean nonSpeech;

    public Phone(String id, int index, boolean silence, boolean filler, boolean nonSpeech) {
        this.id = id;
        this.index = index;
        this.silence = silence;
        this.filler = filler;
        this.nonSpeech = nonSpeech;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Phone phone = (Phone) o;

        return index == phone.index;

    }

    @Override
    public int hashCode() {
        return index;
    }
}
