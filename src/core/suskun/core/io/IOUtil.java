package suskun.core.io;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class IOUtil {
    public static DataInputStream getDataInputStream(Path path) throws IOException {
        return new DataInputStream(new BufferedInputStream(Files.newInputStream(path)));
    }

    public static DataOutputStream getDataOutputStream(Path path) throws IOException {
        return new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)));
    }

    public static DataInputStream getDataInputStream(Path path, int bufferSize) throws IOException {
        if (bufferSize <= 0)
            throw new IllegalArgumentException("Buffer size must be positive. But it is :" + bufferSize);
        return new DataInputStream(new BufferedInputStream(Files.newInputStream(path), bufferSize));
    }

    public static DataOutputStream getDataOutputStream(Path path, int bufferSize) throws IOException {
        if (bufferSize <= 0)
            throw new IllegalArgumentException("Buffer size must be positive. But it is :" + bufferSize);
        return new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path), bufferSize));
    }

    public static int readIntLe(DataInputStream dis) throws IOException {
        return Integer.reverseBytes(dis.readInt());
    }

    public static short readShortLe(DataInputStream dis) throws IOException {
        return Short.reverseBytes(dis.readShort());
    }


}
