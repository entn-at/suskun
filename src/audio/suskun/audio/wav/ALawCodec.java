package suskun.audio.wav;

/**
 * Adapted from public domain C code
 */
public class ALawCodec {

    public static final int MAX_USHORT = ((int) Short.MAX_VALUE) * 2 + 1;    // 65535
    /**
     * An array where the index is the a-law input, and the value is
     * the 16-bit PCM result.
     */
    private static short[] aLawToPcmMap;

    static {
        aLawToPcmMap = new short[256];
        for (int i = 0; i < aLawToPcmMap.length; i++)
            aLawToPcmMap[i] = decodeInit((byte) i);
    }

    private static short decodeInit(byte alaw) {
        //Invert every other bit, and the sign bit (0xD5 = 1101 0101)
        alaw ^= 0xD5;
        //Pull out the value of the sign bit
        int sign = alaw & 0x80;
        //Pull out and shift over the value of the exponent
        int exponent = (alaw & 0x70) >> 4;
        //Pull out the four bits of data
        int data = alaw & 0x0f;
        data <<= 4;
        data += 8;
        //If the exponent is not 0, then we know the four bits followed a 1,
        //and can thus add this implicit 1 with 0x100.
        if (exponent != 0)
            data += 0x100;

        if (exponent > 1)
            data <<= (exponent - 1);

        return (short) (sign == 0 ? data : -data);
    }

    private static final int MAX_ALAW = 0x7fff; //maximum that can be held in 15 bits

    private static byte[] pcmToALawMap;

    static {
        pcmToALawMap = new byte[65536];
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            pcmToALawMap[uShortToInt((short) i)] = encodeInit(i);
        }
    }

    private static byte encodeInit(int pcm) {
        //Get the sign bit.  Shift it for later use without further modification
        int sign = (pcm & 0x8000) >> 8;
        //If the number is negative, make it positive (now it's a magnitude)
        if (sign != 0) {
            pcm = -pcm;
        }
        //The magnitude must fit in 15 bits to avoid overflow
        if (pcm > MAX_ALAW) {
            pcm = MAX_ALAW;
        }

        int exponent = 7;
        //Move to the right and decrement exponent until we hit the 1 or the exponent hits 0
        for (int expMask = 0x4000; (pcm & expMask) == 0 && exponent > 0; expMask >>= 1) {
            exponent--;
        }

        int mantissa = (pcm >> ((exponent == 0) ? 4 : (exponent + 3))) & 0x0f;

        //The a-law byte bit arrangement is SEEEMMMM (Sign, Exponent, and Mantissa.)
        byte alaw = (byte) (sign | exponent << 4 | mantissa);

        //Last is to flip every other bit, and the sign bit (0xD5 = 1101 0101)
        return (byte) (alaw ^ 0xD5);
    }

    /**
     * Encode a pcm value into a a-law byte
     *
     * @param pcm A 16-bit pcm value
     * @return A a-law encoded byte
     */
    public static byte encode(int pcm) {
        return pcmToALawMap[uShortToInt((short) (pcm & 0xffff))];
    }

    /**
     * Encode a pcm value into a a-law byte
     *
     * @param pcm A 16-bit pcm value
     * @return A a-law encoded byte
     */
    public static byte encode(short pcm) {
        return pcmToALawMap[uShortToInt(pcm)];
    }

    private static int uShortToInt(short value) {
        if (value >= 0) {
            return value;
        } else {
            return MAX_USHORT + 1 + value;
        }
    }

    /**
     * Decode one a-law byte
     *
     * @param alaw The encoded a-law byte
     * @return A short containing the 16-bit result
     */
    public static short decode(byte alaw) {
        return aLawToPcmMap[alaw & 0xff];
    }

    /**
     * Decode an a-law encoded byte array.
     *
     * @param alawBytes bytes conatining encoded values
     * @return integer array containing 16 bit short decoded values.
     */
    public static int[] decode(byte[] alawBytes) {
        int result[] = new int[alawBytes.length];
        for (int i = 0; i < alawBytes.length; i++) {
            result[i] = aLawToPcmMap[alawBytes[i] & 0xff];
        }
        return result;
    }

}
