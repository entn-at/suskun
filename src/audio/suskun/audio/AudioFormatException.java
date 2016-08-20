package suskun.audio;

/**
 * Can be thrown when a file with a bad format is being read.
 */
public class AudioFormatException extends RuntimeException {
    public AudioFormatException(String message) {
        super(message);
    }

    public AudioFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
