package suskun.audio.wav;

import com.google.common.io.BaseEncoding;
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

    public static WavHeader fromFile(Path path) throws IOException {

        byte[] buffer4 = new byte[4];
        byte[] buffer2 = new byte[2];
        try (DataInputStream dis = IOUtil.getDataInputStream(path)) {
            dis.readFully(buffer4);
            String chunkId = bytesToString(buffer4);
            if (!chunkId.equalsIgnoreCase("RIFF")) {
                throw new AudioFormatException("Chunk Id = `RIFF` [0x52, 0x49, 0x46, 0x46] is expected but "
                        + chunkId + "[" + BaseEncoding.base16().encode(buffer4) + "]" +
                        " is read.");
            }

            int size = Integer.reverseBytes(dis.readInt());
            if(size<=0) {
                throw new AudioFormatException("RIFF chunk size must be positive. But it is " + size);
            }

            dis.readFully(buffer4);
            String format = bytesToString(buffer4);
            if (!format.equalsIgnoreCase("WAVE")) {
                throw new AudioFormatException("Format = `WAVE` is expected but "
                        + format + "[" + BaseEncoding.base16().encode(buffer4) + "]" +
                        " is read.");
            }






        }
        return null;

    }

    private static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) b);
        }
        return sb.toString();
    }



}
