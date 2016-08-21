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
public class WavChannelReader {

    Path filePath;
    WavHeader header;
    int channel;

    static Logger logger = LoggerFactory.getLogger(WavChannelReader.class);

    public WavChannelReader(Path filePath, int channel) throws IOException {
        this.filePath = filePath;
        this.header = WavHeader.fromFile(filePath);
        this.channel = channel;
    }

    public AudioFormat getFormat() {
        return header.format;
    }

    public float[] loadAll() throws IOException {

        byte[] all = loadAllDataBytes();
        float[] result = new float[header.blockCount()];

        AudioFormat format = header.format;
        if (format.bitsPerSample == 8) {
            for (int i = 0; i < header.blockCount(); i++) {
                byte data = all[i * format.channelCount + channel];
                if (format.format == AudioFormat.Format.A_LAW) {
                    result[i] = ALawCodec.decode(data);
                } else if (format.format == AudioFormat.Format.U_LAW) {
                    result[i] = MuLawCodec.decode(data);
                }
            }
        } else if (format.bitsPerSample == 16) {

            for (int i = 0; i < header.blockCount(); i++) {
                int start = i * 2 * (format.channelCount + channel);
                result[i] = all[start + 1] << 8 | all[start];
            }
        }

        return null;
    }

    private byte[] loadAllDataBytes() throws IOException {
        DataInputStream dis = IOUtil.getDataInputStream(filePath);
        dis.skipBytes(header.dataStart);
        byte[] allData = new byte[header.dataContentSize];
        int actualSize = dis.read(allData);
        if (actualSize != allData.length) {
            logger.warn("Unexpected amount of sample data. [{} expected {} found.] ", allData.length, actualSize);
        }
        return allData;
    }

}
