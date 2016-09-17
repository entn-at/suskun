package suskun.core.io;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class TextUtil {
    public static String loadUtfAsString(Path filePath) throws IOException {
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }

    public static List<String> loadLines(Path path) throws IOException {
        return Files.readAllLines(path, StandardCharsets.UTF_8)
                .stream()
                .filter(s -> s.trim().length() > 0)
                .collect(Collectors.toList());
    }

}
