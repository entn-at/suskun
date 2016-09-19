package suskun.asr.acoustic;

import suskun.nn.QuantizedDnn;
import suskun.core.collections.BidirectionalIndexLookup;
import suskun.core.collections.FloatArrays;
import suskun.core.io.TextUtil;
import suskun.core.math.LogMath;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DnnAcousticModel {

    QuantizedDnn dnn;
    PhoneLookup phoneLookup;
    float[] logAlignmentProbabilities;
    PdfInformation[] transitionToPdfLookup;

    public DnnAcousticModel(QuantizedDnn dnn,
                            PhoneLookup phoneLookup,
                            float[] alignmentCounts,
                            PdfInformation[] transitionToPdfLookup) {
        this.dnn = dnn;
        this.phoneLookup = phoneLookup;

        FloatArrays.scaleInPlace(alignmentCounts, 1f / FloatArrays.sum(alignmentCounts));
        this.logAlignmentProbabilities = LogMath.LINEAR_TO_LOG_FLOAT.convert(alignmentCounts);
        FloatArrays.scaleInPlace(logAlignmentProbabilities, 0.7f);

        this.transitionToPdfLookup = transitionToPdfLookup;
    }

    public QuantizedDnn getDnn() {

        return dnn;
    }

    public DnnAcousticScorer getScorer() {
        return new DnnAcousticScorer(dnn, logAlignmentProbabilities);
    }

    public static DnnAcousticModel loadFromDirectory(Path root) throws IOException {

        // load aligned binary dnn from file.
        Path networkFile = root.resolve("nnet.bin");
        QuantizedDnn dnn = QuantizedDnn.loadFromFile(networkFile.toFile(),3f);

        Path phoneFile = root.resolve("phones.txt");
        PhoneLookup phoneLookup = PhoneLookup.loadFromFile(phoneFile);

        // load pdf alignment counts.
        Path pdfAlignmentCounts = root.resolve("ali_train_pdf.counts");
        String all = TextUtil.loadUtfAsString(pdfAlignmentCounts);
        all = all.replaceAll("\\[|\\]", "").trim();
        float[] alignmentCounts = FloatArrays.fromDelimitedString(all, " ");

        // load transition id to pdf lookup. This file is created with an external tool
        // that uses Kaldi source. it uses transition id information in fst and
        // extracts related phone, hmm state index and pdf index data from gmm model.
        // we need this data to assign pdf (senone) index values from acoustic model
        // to fst transitions.
        Path transitionPdfPath = root.resolve("transition_pdf_lookup");
        PdfInformation[] transitionPdfLookup = loadTransitionPdfLookup(transitionPdfPath, phoneLookup);

        return new DnnAcousticModel(dnn, phoneLookup, alignmentCounts, transitionPdfLookup);
    }

    private static PdfInformation[] loadTransitionPdfLookup(Path transitionPdfPath, PhoneLookup phoneLookup) throws IOException {
        BidirectionalIndexLookup<String> lookup = BidirectionalIndexLookup.fromTextFileWithIndex(transitionPdfPath, '\t');
        PdfInformation[] result = new PdfInformation[lookup.size()];
        for (String key : lookup.keys()) {
            int transitionIndex = lookup.getIndex(key);

            // epsilon requires special treatment. it is not encoded like others.
            if (key.equals("<eps>")) {
                result[transitionIndex] = new PdfInformation(phoneLookup.getPhone("<eps>"), -1, 0);
                continue;
            }

            String[] tokens = key.split("[_]");
            result[transitionIndex] = new PdfInformation(
                    phoneLookup.getPhone(tokens[0]),
                    Integer.parseInt(tokens[2]),
                    Integer.parseInt(tokens[1]));

        }
        return result;
    }

    public PdfInformation pdfInformation(int transitionIndex) {
        return transitionToPdfLookup[transitionIndex];
    }


    public PhoneLookup getPhoneLookup() {
        return phoneLookup;
    }

    public float[] getLogAlignmentProbabilities() {
        return logAlignmentProbabilities;
    }

    public static class PdfInformation {
        Phone phone;
        int pdfIndex;
        int hmmStateIndex;

        public PdfInformation(Phone phone, int pdfIndex, int hmmStateIndex) {
            this.phone = phone;
            this.pdfIndex = pdfIndex;
            this.hmmStateIndex = hmmStateIndex;
        }
    }

    public static void main(String[] args) throws IOException {
        DnnAcousticModel model = loadFromDirectory(Paths.get("../../data/large-16khz"));
    }

}
