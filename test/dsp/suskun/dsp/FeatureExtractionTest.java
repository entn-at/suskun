package suskun.dsp;

import org.junit.Assert;
import org.junit.Test;
import suskun.audio.SpeechData;
import suskun.audio.wav.WavReader;
import suskun.core.FloatData;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FeatureExtractionTest {

    @Test
    public void testKaldiSpectrogram() throws IOException {
        // kaldi spectrogram features.
        // 16KHz, Hamming window, no dither, remove DC offset, use raw energy.
        SpeechData kaldiSpectrogramFeats = SpeechData.loadFromKaldiTxt(
                Paths.get("test/data/feature/kaldi-spectrogram-hamming.feat.text")).get(0);

        // Now we generate with our own.
        Path wavPath = Paths.get("test/data/wav/16khz-16bit-mono.wav");
        FloatData allInput = new FloatData(new WavReader(wavPath, 0).loadAll());

        FrameGenerator generator = FrameGenerator.forTime(16000, 25, 10);

        List<FloatData> frames = generator.getFrames(allInput);

        Assert.assertEquals(kaldiSpectrogramFeats.vectorCount(), frames.size());

        Preprocessor preprocessor =
                Preprocessor.builder(generator.frameSampleSize)
                        .ditherMultiplier(0)
                        .setUseRawEnergy(true)
                        .windowFunctionType(WindowFunction.Function.HAMMING)
                        .build();

        List<Preprocessor.Result> results = preprocessor.processAll(frames);

        EnergySpectrum spectrogram = new EnergySpectrum(new FastFourierTransform(preprocessor.paddedSize), true);

        int i = 0;
        for (Preprocessor.Result result : results) {
            FloatData feature = spectrogram.process(result.data);
            // we remove the first item with energy to match exactly.
            feature.getData()[0] = result.logEnergy;
            FloatData kaldi = kaldiSpectrogramFeats.get(i);
            Assert.assertArrayEquals("Mismatch in frame index " + i + " Expected: " + kaldi.toString() + " Actual:" + feature.toString(),
                    feature.getData(), kaldi.getData(), 0.001f);
            i++;
        }
    }

}
