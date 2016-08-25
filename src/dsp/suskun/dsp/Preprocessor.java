package suskun.dsp;

import com.google.common.math.IntMath;
import suskun.core.FloatData;
import suskun.core.FloatDataProcessor;
import suskun.core.collections.FloatArrays;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Preprocessor {

    public final WindowFunction windowFunction;

    public final Dither dither;

    public final Preemphasis preemphasis;

    public final int inputSize;
    public final int paddedSize;

    public final boolean removeDcOffset;

    // if raw energy is set, energy value is calculated before  preemphasis and windowing,
    // otherwise it is calculated after.
    public final boolean useRawEnergy;

    public static final DcRemover dcRemover = new DcRemover();

    public Preprocessor(Builder builder) {

        this.inputSize = builder.size;

        this.paddedSize = IntMath.isPowerOfTwo(inputSize) ?
                inputSize : IntMath.pow(2, IntMath.log2(inputSize, RoundingMode.UP));

        this.windowFunction = new WindowFunction(builder.windowFunctionType, paddedSize);
        this.removeDcOffset = builder.removeDcOffset;
        this.dither = new Dither(builder.ditherMultiplier);
        this.preemphasis = new Preemphasis(builder.preemphasisFactor);
        this.useRawEnergy = builder.useRawEnergy;
    }

    public static class Builder {
        int size;
        float ditherMultiplier = 0.1f;
        float preemphasisFactor = 0.97f;
        WindowFunction.Function windowFunctionType = WindowFunction.Function.POVEY;
        boolean removeDcOffset = true;
        boolean useRawEnergy = false;

        public Builder(int size) {
            this.size = size;
        }

        public Builder ditherMultiplier(float multiplier) {
            this.ditherMultiplier = multiplier;
            return this;
        }

        public Builder preemphasisFactor(float factor) {
            this.preemphasisFactor = factor;
            return this;
        }

        public Builder windowFunctionType(WindowFunction.Function type) {
            this.windowFunctionType = type;
            return this;
        }

        public Builder setRemoveDcOffset(boolean value) {
            this.removeDcOffset = value;
            return this;
        }

        public Builder setUseRawEnergy(boolean value) {
            this.useRawEnergy = value;
            return this;
        }
    }

    public static class Result {

        public final FloatData data;
        public final float logEnergy;

        public Result(FloatData data, float logEnergy) {
            this.data = data;
            this.logEnergy = logEnergy;
        }
    }

    public Result process(FloatData input) {

        dither.process(input);

        if (removeDcOffset) {
            dcRemover.process(input);
        }

        float logEnergy = 0;
        if (useRawEnergy) {
            logEnergy(input);
        }

        preemphasis.process(input);

        windowFunction.process(input);

        input.resizeInPlace(paddedSize);

        if (!useRawEnergy) {
            logEnergy = logEnergy(input);
        }

        return new Result(input, logEnergy);
    }

    public List<Result> processAll(List<FloatData> input) {
        List<Result> resultList = new ArrayList<>(input.size());
        resultList.addAll(input.stream().map(this::process).collect(Collectors.toList()));
        return resultList;
    }

    public float logEnergy(FloatData input) {
        float energy = FloatArrays.dotProduct(input.getData(), input.getData());
        return (float) Math.log(energy);
    }

    public static class DcRemover implements FloatDataProcessor {

        @Override
        public FloatData process(FloatData data) {
            float mean = FloatArrays.mean(data.getData());
            FloatArrays.addInPlace(data.getData(), mean);
            return data;
        }

        public void processAll(List<FloatData> data) {
            data.forEach(this::process);
        }
    }


}
