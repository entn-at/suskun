package suskun.audio.wav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suskun.core.io.IOUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Used for loading sound content from a wav file.
 */
public class WavReader {

    public final Path filePath;
    public final WavHeader header;
    public final int channel;

    static Logger logger = LoggerFactory.getLogger(WavReader.class);

    public WavReader(Path filePath, int channel) throws IOException {
        this.filePath = filePath;
        this.header = WavHeader.fromFile(filePath);
        if (channel >= header.format.channelCount) {
            throw new IllegalArgumentException(String.format("Wav file does not have channel %d", channel));
        }
        this.channel = channel;
    }

    public AudioFormat getFormat() {
        return header.format;
    }

    /**
     * Loads range of channel samples from the file.
     *
     * @param startSeconds start time in seconds
     * @param endSeconds   end time in seconds
     * @return Samples as float array.
     * @throws IOException
     * @throws IllegalArgumentException If start time is larger than end time.
     */
    public float[] loadRange(double startSeconds, double endSeconds) throws IOException {
        if (startSeconds > endSeconds) {
            throw new IllegalArgumentException(
                    String.format("Start time [%.3f] cannot be higher than end [%.3f] time. ", startSeconds, endSeconds));
        }
        AudioFormat format = header.format;
        byte[] bytes = loadDataBytes(
                format.byteIndexForTime(startSeconds),
                format.byteIndexForTime(endSeconds));
        return getSamples(bytes);
    }

    /**
     * Loads all channel samples from the file.
     *
     * @return Samples as float array.
     * @throws IOException
     */
    public float[] loadAll() throws IOException {

        byte[] all = loadDataBytes(0, header.dataSizeInBytes);
        return getSamples(all);
    }

    private float[] getSamples(byte[] all) {

        int blockCount = all.length / header.format.bytePerBlock;

        float[] result = new float[blockCount];

        AudioFormat format = header.format;
        if (format.bitsPerSample == 8) {
            for (int i = 0; i < blockCount; i++) {
                byte data = all[i * format.channelCount + channel];
                if (format.format == AudioFormat.Format.A_LAW) {
                    result[i] = ALawCodec.decode(data);
                } else if (format.format == AudioFormat.Format.U_LAW) {
                    result[i] = MuLawCodec.decode(data);
                }
            }
        } else if (format.bitsPerSample == 16) {

            for (int i = 0; i < blockCount; i++) {
                int start = i * 2 * (format.channelCount + channel);
                result[i] = all[start + 1] << 8 | all[start];
            }
        }

        return result;
    }

    private byte[] loadDataBytes(int blockStartBytes, int blockEndBytes) throws IOException {

        try (DataInputStream dis = IOUtil.getDataInputStream(filePath)) {

            dis.skipBytes(header.dataStart + blockStartBytes);

            byte[] data = new byte[blockEndBytes - blockStartBytes];
            int actualSize = dis.read(data);
            if (actualSize != data.length) {
                logger.warn("Unexpected amount of sample data. [{} expected {} found.] ", data.length, actualSize);
            }
            return data;
        }
    }
}
