package suskun.asr.acoustic;

import suskun.audio.SpeechData;

public interface FeatureExtractor {
    SpeechData extract(SpeechData input);
}
