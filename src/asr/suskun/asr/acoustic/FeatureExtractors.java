package suskun.asr.acoustic;

import suskun.audio.SpeechData;
import suskun.core.FloatData;
import suskun.dsp.*;

import java.util.List;
import java.util.stream.Collectors;

public class FeatureExtractors {

    public static class BatchWindowDeltaMfccExtractor implements FeatureExtractor {

        DeltaFeatures deltaExtractor;
        WindowFeatures windowExtractor;
        int samplingRate;

        public BatchWindowDeltaMfccExtractor(int samplingRate, int featureSize) {
            this.deltaExtractor = new DeltaFeatures(2, false);
            this.windowExtractor = WindowFeatures.builder(5, 5).setLiveMode(false).paddedSize(featureSize).build();
            this.samplingRate = samplingRate;
        }

        @Override
        public SpeechData extract(SpeechData input) {

            FrameGenerator generator = FrameGenerator.forTime(samplingRate, 25, 10);

            List<FloatData> frames = generator.getFrames(input.get(0));

            Preprocessor preprocessor =
                    Preprocessor.builder(generator.frameSampleSize)
                            .ditherMultiplier(0)
                            .windowFunctionType(WindowFunction.Function.POVEY)
                            .build();

            List<Preprocessor.Result> results = preprocessor.processAll(frames);

            EnergySpectrum spectrogram = new EnergySpectrum(new FastFourierTransform(preprocessor.paddedSize), false);
            MelFilter filter = new MelFilter(16000, 256, 20, 8000, 23);
            MelCepstrum cepstrum = new MelCepstrum(13, 23, 22);
            List<FloatData> mfccFeatures = results.stream().map(result -> cepstrum.process(
                    filter.process(spectrogram.process(result.data)))).collect(Collectors.toList());

            List<FloatData> deltaFeatures = deltaExtractor.get(mfccFeatures);
            List<FloatData> windowFeatures = windowExtractor.get(deltaFeatures);

            return new SpeechData(input.getSegment(), windowFeatures);
        }
    }
}