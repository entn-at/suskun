package suskun.dsp;

import org.junit.Assert;
import org.junit.Test;
import suskun.audio.SpeechData;
import suskun.core.FloatData;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WindowFeaturesTest {

    @Test
    public void test1() {

        float[][] f = {{1, 2}, {3, 4}, {5, 6}, {7, 8}, {9, 10}};

        List<FloatData> data = FloatData.fromArrays(f);

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

        float[][] f = {{1, 2}, {3, 4}, {5, 6}};

        List<FloatData> data = FloatData.fromArrays(f);

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

    static Path featureRoot = Paths.get("test/data/feature");

    @Test
    public void kaldiDeltasFromFile() throws IOException {

        kaldiDeltasFromFile(
                featureRoot.resolve("kaldi-mfcc-40-16khz-cmn-applied.feat.text"),
                featureRoot.resolve("kaldi-mfcc-40-16khz-delta-order2.feat.text"));
    }

    public void kaldiDeltasFromFile(Path featureInput, Path featureOutput) throws IOException {
        SpeechData kaldiCmnFeats = SpeechData.loadFromKaldiTxt(featureInput).get(0);
        SpeechData kaldiDeltaFeats = SpeechData.loadFromKaldiTxt(featureOutput).get(0);

        DeltaFeatures deltaExtractor = new DeltaFeatures(2, false);

        Assert.assertEquals(kaldiCmnFeats.vectorCount(), kaldiDeltaFeats.vectorCount());

        List<FloatData> deltas = deltaExtractor.get(kaldiCmnFeats.getContent());

        Assert.assertEquals(kaldiDeltaFeats.vectorCount(), deltas.size());

        testEquality(kaldiDeltaFeats.getContent(), deltas, 0.001f);
    }

    public static void testEquality(List<FloatData> actualList, List<FloatData> expectedList, float epsilon) {
        Assert.assertEquals(
                String.format("Actual list size %d is not equal to expected list size %d", actualList.size(), expectedList.size()),
                expectedList.size(),
                actualList.size());
        int i = 0;
        for (FloatData expected : expectedList) {
            FloatData actual = actualList.get(i);
            Assert.assertArrayEquals(
                    "Mismatch in data index " + i + "\nExpected: " + expected + "\nActual   :" + actual,
                    expected.getData(), actual.getData(), epsilon);
            i++;
        }
    }


}
