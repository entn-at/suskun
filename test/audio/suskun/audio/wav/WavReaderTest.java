package suskun.audio.wav;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class WavReaderTest {

    @Test
    public void test16KhzMono() throws IOException {

        float[] expectedFirst20 = {
                -33f, -25.0f, -29.0f, -28.0f, -26.0f, -30.0f, -14.0f,
                -5.0f, -27.0f, -23.0f, -6.0f, -24.0f, -56.0f, -38.0f,
                -41.0f, -53.0f, -35.0f, -2.0f, 4.0f, -25.0f};
        float[] expectedLast20 = {
                9.0f, 13.0f, 12.0f, 2.0f, 21.0f, 16.0f, 19.0f,
                3.0f, 17.0f, 26.0f, 23.0f, 11.0f, 12.0f, 25.0f,
                -12.0f, -14.0f, -16.0f, -28.0f, 18.0f, 0.0f
        };
        Path input = Paths.get("test/data/wav/16khz-16bit-mono.wav");
        WavReader reader = new WavReader(input, 0);
        float[] data = reader.loadAll();
        Assert.assertEquals(32047, data.length);
        float[] first20 = Arrays.copyOfRange(data, 0, 20);
        float[] last20 = Arrays.copyOfRange(data, data.length - 20, data.length);

        Assert.assertArrayEquals(expectedFirst20, first20, 0.001f);
        Assert.assertArrayEquals(expectedLast20, last20, 0.001f);
    }

    @Test
    public void test16KhzStereo() throws IOException {

        float[] expectedFirst20 = {
                -33f, -25.0f, -29.0f, -28.0f, -26.0f, -30.0f, -14.0f,
                -5.0f, -27.0f, -23.0f, -6.0f, -24.0f, -56.0f, -38.0f,
                -41.0f, -53.0f, -35.0f, -2.0f, 4.0f, -25.0f};
        float[] expectedLast20 = {
                9.0f, 13.0f, 12.0f, 2.0f, 21.0f, 16.0f, 19.0f,
                3.0f, 17.0f, 26.0f, 23.0f, 11.0f, 12.0f, 25.0f,
                -12.0f, -14.0f, -16.0f, -28.0f, 18.0f, 0.0f
        };
        Path input = Paths.get("test/data/wav/16khz-16bit-stereo.wav");
        WavReader reader = new WavReader(input, 1);
        float[] data = reader.loadAll();
        Assert.assertEquals(32047, data.length);
        float[] first20 = Arrays.copyOfRange(data, 0, 20);
        float[] last20 = Arrays.copyOfRange(data, data.length - 20, data.length);

        Assert.assertArrayEquals(expectedFirst20, first20, 0.001f);
        Assert.assertArrayEquals(expectedLast20, last20, 0.001f);
    }

    @Test
    public void testALawChannel0() throws IOException {

        float[] expectedFirst10 = {
                8.0f, 8.0f, 8.0f, -8.0f, 8.0f,
                8.0f, -8.0f, -8.0f, 8.0f, -8.0f};
        float[] expectedLast10 = {
                8.0f, 8.0f, 8.0f, 8.0f, 8.0f,
                8.0f, 8.0f, 8.0f, 8.0f, 8.0f};
        Path input = Paths.get("test/data/wav/alaw-stereo.wav");
        WavReader reader = new WavReader(input, 0);
        float[] data = reader.loadAll();
        Assert.assertEquals(data.length, 23493);
        float[] first10 = Arrays.copyOfRange(data, 0, 10);
        float[] last10 = Arrays.copyOfRange(data, data.length - 10, data.length);

        Assert.assertArrayEquals(expectedFirst10, first10, 0.001f);
        Assert.assertArrayEquals(expectedLast10, last10, 0.001f);
    }

    @Test
    public void testALawChannel1() throws IOException {

        float[] expectedFirst10 = {
                8.0f, 8.0f, 8.0f, 8.0f, -8.0f,
                8.0f, 8.0f, -8.0f, -8.0f, -8.0f};
        float[] expectedLast10 = {
                8.0f, 8.0f, 8.0f, 8.0f, 8.0f,
                8.0f, -8.0f, -8.0f, -8.0f, -8.0f};
        Path input = Paths.get("test/data/wav/alaw-stereo.wav");
        WavReader reader = new WavReader(input, 1);
        float[] data = reader.loadAll();
        Assert.assertEquals(data.length, 23493);
        float[] first10 = Arrays.copyOfRange(data, 0, 10);
        float[] last10 = Arrays.copyOfRange(data, data.length - 10, data.length);

        Assert.assertArrayEquals(expectedFirst10, first10, 0.001f);
        Assert.assertArrayEquals(expectedLast10, last10, 0.001f);
    }

    @Test
    public void testMuLawChannel0() throws IOException {
        float[] expectedFrom500 = {
                -4860.0f, -4348.0f, -652.0f, 3388.0f, 556.0f,
                1180.0f, 2364.0f, -2108.0f, -4092.0f, -3772.0f};
        float[] expectedFrom18000 = {
                80.0f, 80.0f, 48.0f, -40.0f, -40.0f,
                80.0f, 64.0f, 32.0f, 24.0f, -64.0f};
        Path input = Paths.get("test/data/wav/mulaw-stereo.wav");
        WavReader reader = new WavReader(input, 0);
        float[] data = reader.loadAll();
        Assert.assertEquals(data.length, 23493);
        float[] from500 = Arrays.copyOfRange(data, 500, 510);
        float[] from18000 = Arrays.copyOfRange(data, 18000, 18010);

        Assert.assertArrayEquals(expectedFrom500, from500, 0.001f);
        Assert.assertArrayEquals(expectedFrom18000, from18000, 0.001f);
    }


}
