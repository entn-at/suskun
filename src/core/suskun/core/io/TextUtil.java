package suskun.core.io;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextUtil {
    public static String loadUtfAsString(Path filePath) throws IOException {
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }

}
