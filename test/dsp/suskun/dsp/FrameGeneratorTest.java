package suskun.dsp;

import org.junit.Assert;
import org.junit.Test;
import suskun.core.FloatData;

import java.util.List;

public class FrameGeneratorTest {
    @Test
    public void testNoLeftOver() {
        float[] input = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        FrameGenerator generator = FrameGenerator.forTime(10, 500, 200);
        Assert.assertEquals(10, generator.samplingRate);
        Assert.assertEquals(5, generator.frameSampleSize);
        Assert.assertEquals(2, generator.shiftSampleSize);

        List<FloatData> frames = generator.getFrames(new FloatData(input));

        Assert.assertEquals(4, frames.size());
        Assert.assertArrayEquals(new float[]{1, 2, 3, 4, 5}, frames.get(0).getData(), 0.0001f);
        Assert.assertArrayEquals(new float[]{3, 4, 5, 6, 7}, frames.get(1).getData(), 0.0001f);
        Assert.assertArrayEquals(new float[]{5, 6, 7, 8, 9}, frames.get(2).getData(), 0.0001f);
        Assert.assertArrayEquals(new float[]{7, 8, 9, 10, 11}, frames.get(3).getData(), 0.0001f);
    }

    @Test
    public void testwithLeftOver() {
        float[] input = {1, 2, 3, 4, 5, 6, 7, 8};
        FrameGenerator generator = FrameGenerator.forTime(10, 500, 200);
        List<FloatData> frames = generator.getFrames(new FloatData(input));

        Assert.assertEquals(2, frames.size());
        Assert.assertArrayEquals(new float[]{1, 2, 3, 4, 5}, frames.get(0).getData(), 0.0001f);
        Assert.assertArrayEquals(new float[]{3, 4, 5, 6, 7}, frames.get(1).getData(), 0.0001f);

        // now we feed new input
        frames = generator.getFrames(new FloatData(new float[]{9, 10, 11}));
        Assert.assertEquals(2, frames.size());
        Assert.assertArrayEquals(new float[]{5, 6, 7, 8, 9}, frames.get(0).getData(), 0.0001f);
        Assert.assertArrayEquals(new float[]{7, 8, 9, 10, 11}, frames.get(1).getData(), 0.0001f);
    }

}
