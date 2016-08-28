package suskun.audio;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpeechSegmentTest {

    @Test
    public void testFromWav() throws IOException {
        Path wavPath = Paths.get("test/data/wav/16khz-16bit-mono.wav");
        SpeechSegment segment = SpeechSegment.fromWavFile(wavPath, 0);
        Assert.assertEquals(0, segment.source.channel);
        Assert.assertEquals(16000, segment.source.format.sampleRate);
        Assert.assertEquals(2, segment.source.format.bytePerSample);
        Assert.assertEquals(0, segment.startSeconds, 0.0001);
        Assert.assertEquals(2.0029, segment.endSeconds, 0.001);
        Assert.assertEquals(2.0029, segment.durationInSeconds(), 0.001);
    }

    @Test
    public void testLoadData() throws IOException {
        Path wavPath = Paths.get("test/data/wav/16khz-16bit-mono.wav");
        SpeechSegment segment = SpeechSegment.fromWavFile(wavPath, 0);
        float[] data = segment.getSegmentData(wavPath);
        Assert.assertEquals(32047, data.length);
    }
}
