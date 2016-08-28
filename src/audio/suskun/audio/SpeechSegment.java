package suskun.audio;

import com.google.common.base.Preconditions;
import suskun.audio.wav.WavHeader;
import suskun.audio.wav.WavReader;

import java.io.IOException;
import java.nio.file.Path;

public class SpeechSegment {

    String id;
    float startSeconds;
    float endSeconds;
    SpeechSource source;

    public SpeechSegment(String id, float startSeconds, float endSeconds, SpeechSource source) {
        Preconditions.checkArgument(startSeconds >= 0, "Start seconds [%.3f] cannot be negative.", startSeconds);
        Preconditions.checkArgument(endSeconds >= 0, "End seconds [%.3f] cannot be negative.", endSeconds);
        Preconditions.checkArgument(startSeconds <= endSeconds,
                "Start seconds [%.3f] cannot be larger than end seconds value [%.3f]", startSeconds, endSeconds);
        Preconditions.checkNotNull(source);
        this.id = id;
        this.startSeconds = startSeconds;
        this.endSeconds = endSeconds;
        this.source = source;
    }

    public static SpeechSegment unknown(String id) {
        return new SpeechSegment(id, 0, 0, null);
    }

    public static SpeechSegment unknown(String id, SpeechSource source) {
        return new SpeechSegment(id, 0, 0, source);
    }


    public static String wavFileId(Path path) {
        return path.toFile().getName().replaceAll("\\.wav|\\.WAV", "");
    }

    public static SpeechSegment fromWavFile(Path wavFilePath, int channel) throws IOException {
        WavHeader header = WavHeader.fromFile(wavFilePath);
        String id = wavFileId(wavFilePath);
        SpeechSource source = new SpeechSource(id, header.format, channel);
        return new SpeechSegment(id, 0, (float) header.durationInSeconds(), source);
    }

    public float durationInSeconds() {
        return endSeconds - startSeconds;
    }

    public float[] getSegmentData(Path wavFilePath) throws IOException {
        WavReader wavReader = new WavReader(wavFilePath, source.channel);
        if (wavReader.getFormat().sampleRate != this.source.format.sampleRate) {
            throw new IllegalStateException(String.format(
                    "wav File [%s] sampling rate [%d] is not compatible with Segment %s.",
                    wavFilePath, wavReader.getFormat().sampleRate, this.toString()));
        }
        return wavReader.loadRange(startSeconds, endSeconds);
    }

    @Override
    public String toString() {
        return String.format("%s [c=%d %.1f-%.1f]",
                id,
                this.source.channel,
                startSeconds,
                endSeconds);
    }
}
