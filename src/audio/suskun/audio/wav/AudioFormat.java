package suskun.audio.wav;

import suskun.audio.AudioFormatException;

import java.io.DataInputStream;
import java.io.IOException;

import static suskun.core.io.IOUtil.readIntLe;
import static suskun.core.io.IOUtil.readShortLe;

public class AudioFormat {

    public final Format format;
    public final int channelCount;
    public final int sampleRate;
    public final int byteRate;
    public final int bytePerSample;
    public final int bytePerBlock;
    public final int bitsPerSample;
    public final boolean littleEndian;

    enum Format {
        PCM(1),
        IEEE_FLOAT(3),
        A_LAW(6),
        U_LAW(7);

        int wavValue;

        Format(int wavValue) {
            this.wavValue = wavValue;
        }

        static Format getFromWav(int value) {
            switch (value) {
                case 1:
                    return PCM;
                case 3:
                    return IEEE_FLOAT;
                case 6:
                    return A_LAW;
                case 7:
                    return U_LAW;
                default:
                    throw new AudioFormatException("Unsupported audio format value :" + value);
            }
        }
    }

    public AudioFormat(
            Format format,
            int channelCount,
            int sampleRate,
            int byteRate,
            int bytePerBlock,
            int bitsPerSample,
            boolean littleEndian) {
        this.format = format;
        this.channelCount = channelCount;
        this.sampleRate = sampleRate;
        this.byteRate = byteRate;
        this.bytePerSample = bitsPerSample/8;
        this.bytePerBlock = bytePerBlock;
        this.bitsPerSample = bitsPerSample;
        this.littleEndian = littleEndian;
    }

    public static AudioFormat fromWavStream(DataInputStream dis) throws IOException {
        int format = readShortLe(dis);
        int channelCount = readShortLe(dis);
        int sampleRate = readIntLe(dis);
        int byteRate = readIntLe(dis);
        int blockAlign = readShortLe(dis);
        int bitsPerSample = readShortLe(dis);

        return new AudioFormat(
                Format.getFromWav(format),
                channelCount,
                sampleRate,
                byteRate,
                blockAlign, bitsPerSample, true);

    }


}
