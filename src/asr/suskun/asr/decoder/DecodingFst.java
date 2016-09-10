package suskun.asr.decoder;

import suskun.core.collections.UIntIntMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DecodingFst {


    static class Node {
        int state;
        int [] targetStates;
        float [] scores;
        int pronunciationId;
    }

    public static DecodingFst loadFromText(Path fstPath) throws IOException {

        BufferedReader reader = Files.newBufferedReader(fstPath, StandardCharsets.UTF_8);
        String line;
        int counter = 0;
        UIntIntMap tokenHistogram = new UIntIntMap();
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("[\t]");
            int tokenCount = tokens.length;
            tokenHistogram.increment(tokenCount, 1);
            counter++;
            if (counter % 50000 == 0) {
                System.out.println(counter);
            }
            if(tokenCount<=2) {
                System.out.println(line);
            }
        }

        for (int key : tokenHistogram.getKeysSorted()) {
            System.out.println(key + " " + tokenHistogram.get(key));
        }
        tokenHistogram.getKeysSorted().forEach(System.out::println);

        return null;

    }

}
