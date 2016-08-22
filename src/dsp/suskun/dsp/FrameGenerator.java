package suskun.dsp;

/**
 * Used for generating shifted frames.
 */
public class FrameGenerator {

    int samplingRate;

    float frameLength;
    float shiftLength;
    int frameSampleSize;
    int shiftSampleSize;

    private FrameGenerator(int samplingRate, float frameLength, float shiftLength) {
        this.samplingRate = samplingRate;
        this.frameLength = frameLength;
        this.shiftLength = shiftLength;
        this.frameSampleSize = (int) (samplingRate / frameLength * 1000d);
        this.shiftSampleSize = (int) (shiftLength / frameLength * 1000d);
    }

    /**
     * Generates an instance.
     *
     * @param samplingRate    sampling Rate in hertz.
     * @param frameLengthInMs frame length in milliseconds. 25ms is typical.
     * @param shiftInMs       frame shift in milliseconds. 10ms is typical.
     * @return
     */
    public static FrameGenerator forTime(int samplingRate, float frameLengthInMs, float shiftInMs) {
        return new FrameGenerator(samplingRate, frameLengthInMs, shiftInMs);
    }


}
