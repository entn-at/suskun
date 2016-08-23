package suskun.dsp;

import suskun.core.FloatData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used for generating shifted frames.
 * Instances are stateful as they carry frame indexes and leftover data.
 */
public class FrameGenerator {

    public final int samplingRate;

    public final float frameLength;
    public final float shiftLength;
    public final int frameSampleSize;
    public final int shiftSampleSize;

    int frameCounter = 0;
    FloatData leftover = FloatData.empty();

    private FrameGenerator(int samplingRate, float frameLength, float shiftLength) {
        this.samplingRate = samplingRate;
        this.frameLength = frameLength;
        this.shiftLength = shiftLength;
        this.frameSampleSize = (int) (samplingRate * frameLength / 1000d);
        this.shiftSampleSize = (int) (samplingRate * shiftLength / 1000d);
    }


    public int getFrameCounter() {
        return frameCounter;
    }

    /**
     * Generates an instance.
     *
     * @param samplingRate    sampling Rate in hertz.
     * @param frameLengthInMs frame length in milliseconds. 25ms is typical.
     * @param shiftInMs       frame shift in milliseconds. 10ms is typical.
     * @return a FrameGenerator instance.
     */
    public static FrameGenerator forTime(int samplingRate, float frameLengthInMs, float shiftInMs) {
        return new FrameGenerator(samplingRate, frameLengthInMs, shiftInMs);
    }

    public List<FloatData> getFrames(FloatData input) {

        // If there is any previous unprocessed data, prepend it to the incoming data
        if (!leftover.isEmpty()) {
            input.prependInPlace(leftover.getData());
        }

        // If size is smaller than frame size, set input as leftover and return empty.
        if (input.length() < frameSampleSize) {
            leftover = input;
            return Collections.emptyList();
        }

        // generate frames.
        int startIndex = 0;
        int endIndex = frameSampleSize;
        List<FloatData> frames = new ArrayList<>(input.length() / shiftSampleSize);
        while (endIndex <= input.length()) {
            float[] frameData = new float[frameSampleSize];
            System.arraycopy(input.getData(), startIndex, frameData, 0, frameSampleSize);
            frames.add(new FloatData(frameCounter++, frameData));
            startIndex += shiftSampleSize;
            endIndex += shiftSampleSize;
        }
        if (endIndex > frameSampleSize) {
            float[] leftOverData = new float[input.length() - startIndex];
            System.arraycopy(input.getData(), startIndex, leftOverData, 0, leftOverData.length);
            this.leftover = new FloatData(0, leftOverData);
        } else {
            this.leftover = FloatData.empty();
        }
        return frames;
    }


}
