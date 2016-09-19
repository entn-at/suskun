package suskun.asr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suskun.asr.acoustic.DnnAcousticModel;
import suskun.asr.acoustic.FeatureExtractor;
import suskun.asr.acoustic.FeatureExtractors;
import suskun.audio.SpeechData;

import java.io.IOException;
import java.nio.file.Paths;

public class TestOfflineRecognition {

    static Logger logger = LoggerFactory.getLogger(TestOfflineRecognition.class);

    public static void main(String[] args) throws IOException {
        logger.info("Loading acoustic model.");
        DnnAcousticModel acousticModel = DnnAcousticModel.loadFromDirectory(Paths.get("../../data/large-16khz"));

        logger.info("Loading wav data.");
        SpeechData wavSamples = SpeechData.fromWavfile(Paths.get("test/data/wav/16khz-16bit-mono.wav"));

        logger.info("Extract features.");
        FeatureExtractor extractor =
                new FeatureExtractors.BatchWindowDeltaMfccExtractor(16000, acousticModel.getDnn().inputDimension());

        SpeechData features = extractor.extract(wavSamples);

        logger.info("Score features.");
        SpeechData acousticScores = acousticModel.getScorer().score(features);

        logger.info("Decode.");

        // decode..


    }
}
