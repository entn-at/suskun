package suskun.audio.wav;

/**
 * Adapted from public domain C code
 */

public class MuLawCodec {

    public static final int BIAS_MULAW = 0x84; //aka 132, or 1000 0100
    public static final int MAX_MULAW = 32635; //32767 (max 15-bit integer) minus BIAS

    /**
     * An array where the index is the 16-bit PCM input, and the value is the mu-law result.
     */
    private static byte[] pcmToMuLawMap;

    static {
        pcmToMuLawMap = new byte[65536];
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++)
            pcmToMuLawMap[uShortToInt((short) i)] = encodeInit(i);
    }

    /**
     * Encode one mu-law byte from a 16-bit signed integer. Internal use only.
     *
     * @param pcm A 16-bit signed pcm value
     * @return A mu-law encoded byte
     */
    private static byte encodeInit(int pcm) {
        //Get the sign bit.  Shift it for later use without further modification
        int sign = (pcm & 0x8000) >> 8;
        //If the number is negative, make it positive (now it's a magnitude)
        if (sign != 0) {
            pcm = -pcm;
        }
        //The magnitude must be less than 32635 to avoid overflow
        if (pcm > MAX_MULAW) {
            pcm = MAX_MULAW;
        }
        //Add 132 to guarantee a 1 in the eight bits after the sign bit
        pcm += BIAS_MULAW;

        int exponent = 7;
        //Move to the right and decrement exponent until we hit the 1
        for (int expMask = 0x4000; (pcm & expMask) == 0; expMask >>= 1) {
            exponent--;
        }

        int mantissa = (pcm >> (exponent + 3)) & 0x0f;

        //The mu-law byte bit arrangement is SEEEMMMM (Sign, Exponent, and Mantissa.)
        byte mulaw = (byte) (sign | exponent << 4 | mantissa);

        //Last is to flip the bits
        return (byte) ~mulaw;
    }

    /**
     * Encode a pcm value into a mu-law byte
     *
     * @param pcm A 16-bit pcm value
     * @return A mu-law encoded byte
     */
    public static byte encode(int pcm) {
        return pcmToMuLawMap[pcm & 0xffff];
    }

    /**
     * Encode a pcm value into a mu-law byte
     *
     * @param pcm A 16-bit pcm value
     * @return A mu-law encoded byte
     */
    public static byte encode(short pcm) {
        return pcmToMuLawMap[uShortToInt(pcm)];
    }

    /**
     * An array where the index is the mu-law input, and the value is
     * the 16-bit PCM result.
     */
    private static short[] muLawToPcmMap;

    static {
        muLawToPcmMap = new short[256];
        for (short i = 0; i < muLawToPcmMap.length; i++)
            muLawToPcmMap[i] = decodeMuLaw((byte) i);
    }

    /**
     * Decode one mu-law byte. For internal use only.
     *
     * @param mulaw The encoded mu-law byte
     * @return A short containing the 16-bit result
     */
    private static short decodeMuLaw(byte mulaw) {
        //Flip all the bits
        mulaw = (byte) ~mulaw;

        //Pull out the value of the sign bit
        int sign = mulaw & 0x80;
        //Pull out and shift over the value of the exponent
        int exponent = (mulaw & 0x70) >> 4;
        //Pull out the four bits of data
        int data = mulaw & 0x0f;

        //Add on the implicit fifth bit (we know the four data bits followed a one bit)
        data |= 0x10;
        /* Add a 1 to the end of the data by shifting over and adding one.  Why?
         * Mu-law is not a one-to-one function.  There is a range of values that all
         * map to the same mu-law byte.  Adding a one to the end essentially adds a
         * "half byte", which means that the decoding will return the value in the
         * middle of that range.  Otherwise, the mu-law decoding would always be
         * less than the original data. */
        data <<= 1;
        data += 1;
        /* Shift the five bits to where they need to be: left (exponent + 2) places
         * Why (exponent + 2) ?
         * 1 2 3 4 5 6 7 8 9 A B C D E F G
         * . 7 6 5 4 3 2 1 0 . . . . . . . <-- starting bit (based on exponent)
         * . . . . . . . . . . 1 x x x x 1 <-- our data
         * We need to move the one under the value of the exponent,
         * which means it must move (exponent + 2) times
         */
        data <<= exponent + 2;
        //Remember, we added to the original, so we need to subtract from the final
        data -= BIAS_MULAW;
        //If the sign bit is 0, the number is positive. Otherwise, negative.
        return (short) (sign == 0 ? data : -data);
    }

    /**
     * Decode one mu-law byte
     *
     * @param mulaw The encoded mu-law byte
     * @return A short containing the 16-bit result
     */
    public static short decode(byte mulaw) {
        return muLawToPcmMap[mulaw & 0xff];
    }

    public static final int MAX_USHORT = ((int) Short.MAX_VALUE) * 2 + 1;    // 65535

    private static int uShortToInt(short value) {
        if (value >= 0)
            return value;
        else
            return MAX_USHORT + 1 + value;
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
            result[i] = muLawToPcmMap[alawBytes[i] & 0xff];
        }
        return result;
    }
}
