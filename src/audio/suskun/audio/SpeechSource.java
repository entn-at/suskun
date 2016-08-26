package suskun.audio;

import suskun.audio.wav.AudioFormat;

public class SpeechSource {

    String id;
    AudioFormat format;
    int channel;

    public SpeechSource(String id, AudioFormat format, int channel) {
        this.id = id;
        this.format = format;
        this.channel = channel;
    }
}
