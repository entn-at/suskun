package suskun.asr.acoustic;

import suskun.asr.nn.QuantizedDnn;
import suskun.core.collections.FloatArrays;
import suskun.core.io.TextUtil;
import suskun.core.math.LogMath;

import java.io.IOException;
import java.nio.file.Path;

public class DnnAcousticModel {

    QuantizedDnn dnn;
    PhoneLookup phoneLookup;
    float[] logAlignmentProbabilities;

    public DnnAcousticModel(QuantizedDnn dnn, PhoneLookup phoneLookup, float[] alignmentCounts) {
        this.dnn = dnn;
        this.phoneLookup = phoneLookup;

        FloatArrays.scaleInPlace(alignmentCounts, 1f / FloatArrays.sum(alignmentCounts));
        this.logAlignmentProbabilities = LogMath.LINEAR_TO_LOG_FLOAT.convert(alignmentCounts);
        FloatArrays.scaleInPlace(logAlignmentProbabilities, 0.7f);
    }

    public static DnnAcousticModel loadFromDirectory(Path path) throws IOException {

        // load aligned binary dnn from file.
        Path networkFile = path.resolve("nnet.bin");
        QuantizedDnn dnn = QuantizedDnn.loadFromFile(networkFile.toFile());

        Path phoneFile = path.resolve("phones.txt");
        PhoneLookup lookup = PhoneLookup.loadFromFile(path);

        // load pdf alignment counts.
        Path pdfAlignmentCounts = path.resolve("ali_train_pdf.counts");
        String all = TextUtil.loadUtfAsString(pdfAlignmentCounts);
        all = all.replaceAll("\\[\\]", "").trim();
        float[] alignmentCounts = FloatArrays.fromDelimitedString(all, " ");

        return new DnnAcousticModel(dnn, lookup, alignmentCounts);
    }

    public PhoneLookup getPhoneLookup() {
        return phoneLookup;
    }

    public float[] getLogAlignmentProbabilities() {
        return logAlignmentProbabilities;
    }
}
