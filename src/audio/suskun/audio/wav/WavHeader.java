package suskun.audio.wav;

import suskun.audio.AudioFormatException;
import suskun.core.io.IOUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents the wav sound file header. Header is in RIFF format.
 * <p>
 * Format is as follows:
 * <pre>
 * `RIFF`            4 Bytes ASCII Big Endian (BE).
 * [Content Size]    4 Bytes Little Endian (LE). Actual amount is the file size minus 8 bytes.
 * `WAVE`            4 Bytes ASCII BE.
 * `fmt `            4 Bytes ASCII BE. Audio Format <-- Sub chunk ID
 * [Chunk Size]      4 Bytes LE. Amount of bytes for the sub-chunk after this
 * [Audio Format]    2 Bytes LE. PCM = 1, IEEE Float = 3, A-Law = 6, u-law = 7
 * [Channel Count]   2 Bytes LE
 * [Sample Rate]     4 Bytes LE. In Hertz
 * [Byte Rate]       4 Bytes LE. SampleRate * NumChannels * BitsPerSample/8
 * [Block Align]     2 Bytes LE. NumChannels * BitsPerSample/8 The number of bytes for one sample including
 *                   all channels.
 * [Bits Per Sample] 2 Bytes LE. 8  16 bits
 * `data`            4 Bytes ASCII BE.
 * [Chunk Size]      4 Bytes LE. NumSamples * NumChannels * BitsPerSample/8
 * [Data]            LE Interleaved samples. For example For 16 bit 2 channels, Data is
 *                   Ch1-1-1 Ch1-1-0 Ch2-1-1 Ch2-1-0 Ch1-2-1 Ch1-2-0 Ch2-2-1 Ch2-2-0
 *                   Where [Channel-sample-byte]
 * </pre>
 * <p>
 * Chunks may not be ordered like the example above. This class will omit chunks other than `data` and `fmt`.
 */

public class WavHeader {

    /**
     * Format data
     */
    public final AudioFormat format;

    /**
     * Start of the data chunk content. This will be used for loading the actual data.
     */
    public final int dataStart;

    /**
     * Amount of bytes in data chunk content.
     */
    public final int dataSizeInBytes;

    private WavHeader(AudioFormat format, int dataStart, int dataSizeInBytes) {
        this.format = format;
        this.dataStart = dataStart;
        this.dataSizeInBytes = dataSizeInBytes;
    }

    public static WavHeader fromFile(Path path) throws IOException {

        int byteCounter = 0;

        try (DataInputStream dis = IOUtil.getDataInputStream(path)) {

            // check `RIFF` and size.
            String chunkId = readAsciiBe(dis);
            if (!chunkId.equalsIgnoreCase("RIFF")) {
                throw new AudioFormatException("Chunk Id = `RIFF` [0x52, 0x49, 0x46, 0x46] is expected but "
                        + chunkId + " is read.");
            }

            int size = IOUtil.readIntLe(dis);
            if (size <= 0) {
                throw new AudioFormatException("RIFF chunk size must be positive. But it is " + size);
            }

            // check RIFF format. Should be `WAVE`
            String riffFormat = readAsciiBe(dis);
            if (!riffFormat.equalsIgnoreCase("WAVE")) {
                throw new AudioFormatException("Format = `WAVE` is expected but "
                        + riffFormat + " is read.");
            }

            // bytes load so far.
            byteCounter += 4 + 4 + 4;

            boolean fmtFound = false, dataFound = false;
            AudioFormat format = null;
            int dataStart = 0;
            int dataContentSize = 0;

            while (!fmtFound || !dataFound) {

                chunkId = readAsciiBe(dis);
                size = IOUtil.readIntLe(dis);

                byteCounter += 4 + 4;

                if (chunkId.equalsIgnoreCase("fmt ")) {
                    format = AudioFormat.fromWavStream(dis);
                    // sometimes fmt has more data. we ignore it.
                    if (size > 16) {
                        dis.skipBytes(size - 16);
                    }
                    fmtFound = true;
                } else if (chunkId.equalsIgnoreCase("data")) {
                    dataStart = byteCounter;
                    dataContentSize = size;
                    dataFound = true;
                    dis.skipBytes(size);
                } else {
                    dis.skipBytes(size);
                }
                byteCounter += size;
            }
            return new WavHeader(format, dataStart, dataContentSize);
        }
    }

    private static String readAsciiBe(DataInputStream dis) throws IOException {
        byte[] buffer4 = new byte[4];
        dis.readFully(buffer4);
        return bytesToString(buffer4);
    }

    private static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) b);
        }
        return sb.toString();
    }

    /**
     * @return Total duration in seconds.
     */
    public double durationInSeconds() {
        return format.durationInSeconds(blockCount());
    }

    public int blockCount() {
        return dataSizeInBytes / format.bytePerBlock;
    }

}
