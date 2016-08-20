package suskun.core.io;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class IOUtil {
    public static DataInputStream getDataInputStream(Path path) throws IOException {
        return new DataInputStream(new BufferedInputStream(Files.newInputStream(path)));
    }

    public static DataInputStream getDataInputStream(Path path, int bufferSize) throws IOException {
        if (bufferSize <= 0)
            throw new IllegalArgumentException("Buffer size must be positive. But it is :" + bufferSize);
        return new DataInputStream(new BufferedInputStream(Files.newInputStream(path)));
    }


}
