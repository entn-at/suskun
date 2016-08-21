package suskun.audio.wav;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class WavHeaderTest {

    @Test
    public void mono16KhzFunctionalTest() throws IOException {
        WavHeader header = WavHeader.fromFile(Paths.get("test/data/wav/16khz-16bit-mono.wav"));
        Assert.assertNotNull(header.format);
        AudioFormat format = header.format;

        Assert.assertEquals(AudioFormat.Format.PCM, format.format);
        Assert.assertEquals(1, format.channelCount);
        Assert.assertEquals(16000, format.sampleRate);
        Assert.assertEquals(16, format.bitsPerSample);
        Assert.assertEquals(2, format.bytePerBlock);
        Assert.assertEquals(32000, format.byteRate);
        Assert.assertEquals(true, format.littleEndian);
    }

    @Test
    public void
    aLawStereo8KhzFunctionalTest() throws IOException {
        WavHeader header = WavHeader.fromFile(Paths.get("test/data/wav/alaw-stereo.wav"));
        Assert.assertNotNull(header.format);
        AudioFormat format = header.format;

        Assert.assertEquals(AudioFormat.Format.A_LAW, format.format);
        Assert.assertEquals(2, format.channelCount);
        Assert.assertEquals(8000, format.sampleRate);
        Assert.assertEquals(8, format.bitsPerSample);
        Assert.assertEquals(2, format.bytePerBlock);
        Assert.assertEquals(16000, format.byteRate);
        Assert.assertEquals(true, format.littleEndian);
    }

}
