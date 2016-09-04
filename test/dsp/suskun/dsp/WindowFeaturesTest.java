package suskun.dsp;

import com.beust.jcommander.internal.Lists;
import org.junit.Assert;
import org.junit.Test;
import suskun.core.FloatData;

import java.util.List;

public class WindowFeaturesTest {

    @Test
    public void test1() {

        float[] f1 = {1, 2};
        float[] f2 = {3, 4};
        float[] f3 = {5, 6};
        float[] f4 = {7, 8};
        float[] f5 = {9, 10};

        List<FloatData> data = Lists.newArrayList(
                new FloatData(0, f1.clone()),
                new FloatData(0, f2.clone()),
                new FloatData(0, f3.clone()),
                new FloatData(0, f4.clone()),
                new FloatData(0, f5.clone())
        );

        WindowFeatures features = WindowFeatures.builder(2, 1).build();
        List<FloatData> result = features.get(data);

        Assert.assertEquals(data.size(), result.size());

        float[][] expected = {
                {1, 2, 1, 2, 1, 2, 3, 4},
                {1, 2, 1, 2, 3, 4, 5, 6},
                {1, 2, 3, 4, 5, 6, 7, 8},
                {3, 4, 5, 6, 7, 8, 9, 10},
                {5, 6, 7, 8, 9, 10, 9, 10}};

        for (int i = 0; i < expected.length; i++) {
            float[] floats = expected[i];
            Assert.assertArrayEquals(floats, result.get(i).getData(), 0.0001f);
        }

    }

    @Test
    public void testWithPadding() {

        float[] f1 = {1, 2};
        float[] f2 = {3, 4};
        float[] f3 = {5, 6};

        List<FloatData> data = Lists.newArrayList(
                new FloatData(0, f1.clone()),
                new FloatData(0, f2.clone()),
                new FloatData(0, f3.clone())
        );

        WindowFeatures features = WindowFeatures.builder(2, 1).paddedSize(10).build();
        List<FloatData> result = features.get(data);

        Assert.assertEquals(data.size(), result.size());

        float[][] expected = {
                {1, 2, 1, 2, 1, 2, 3, 4, 0, 0},
                {1, 2, 1, 2, 3, 4, 5, 6, 0, 0},
                {1, 2, 3, 4, 5, 6, 5, 6, 0, 0},
        };

        for (int i = 0; i < expected.length; i++) {
            float[] floats = expected[i];
            Assert.assertArrayEquals(floats, result.get(i).getData(), 0.0001f);
        }

    }
}
