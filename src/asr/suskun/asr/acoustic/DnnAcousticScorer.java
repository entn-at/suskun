package suskun.asr.acoustic;

import suskun.audio.SpeechData;
import suskun.core.collections.FloatArrays;
import suskun.core.math.LogMath;
import suskun.nn.QuantizedDnn;

public class DnnAcousticScorer {

    QuantizedDnn dnn;
    float[] logAlignmentProbabilities;

    public DnnAcousticScorer(QuantizedDnn dnn, float[] logAlignmentProbabilities) {
        this.dnn = dnn;
        this.logAlignmentProbabilities = logAlignmentProbabilities;
    }

    public SpeechData score(SpeechData speechData) {
        float[][] input = speechData.getContentAsMatrix();
        float[][] result = dnn.calculate(input);
        for (float[] vec : result) {
            LogMath.LINEAR_TO_LOG_FLOAT.convertInPlace(vec);
            FloatArrays.subtractFromFirst(vec, logAlignmentProbabilities);
        }
        return new SpeechData(speechData.getSegment(), result);
    }
}
