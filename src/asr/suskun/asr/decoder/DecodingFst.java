package suskun.asr.decoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suskun.core.io.IOUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DecodingFst {

    static final Logger logger = LoggerFactory.getLogger(DecodingFst.class);

    public static void convertBinary(Path fstPath, Path binaryPath) throws IOException {

        try (DataOutputStream dos = IOUtil.getDataOutputStream(binaryPath, 1 << 14)) {
            BufferedReader reader = Files.newBufferedReader(fstPath, StandardCharsets.UTF_8);

            int counter = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                String[] tokens = line.split("[\t]");
                int tokenCount = tokens.length;

                dos.writeByte(tokenCount);
                // there is always a state id.
                int state = Integer.parseInt(tokens[0]);
                dos.writeInt(state);

                switch (tokenCount) {
                    case 1:
                        // this is the terminal state. we already saved it.
                        break;
                    case 2:
                        // write state with score.
                        dos.writeFloat(Float.parseFloat(tokens[1]));
                        break;
                    case 4:
                        dos.writeInt(Integer.parseInt(tokens[1])); // target state
                        dos.writeInt(Integer.parseInt(tokens[2])); // transition id
                        dos.writeInt(Integer.parseInt(tokens[3])); // word id
                        break;
                    case 5:
                        dos.writeInt(Integer.parseInt(tokens[1])); // target state
                        dos.writeInt(Integer.parseInt(tokens[2])); // transition id
                        dos.writeInt(Integer.parseInt(tokens[3])); // word id
                        dos.writeFloat(Float.parseFloat(tokens[4])); // score
                        break;
                    default:
                        throw new IllegalStateException("Cannot identify line : " + line);
                }
                if (counter % 1000000 == 0) {
                    logger.info("Line processed so far = {}", counter);
                }
                counter++;
            }
            // to mark end of tokens.
            dos.writeByte(-1);
        }
    }


    public static void convertRawBinary(Path fstPath, Path outRoot) throws IOException {

        Files.createDirectories(outRoot);
        Path offsetPath = outRoot.resolve("offsets.bin");
        Path dataPath = outRoot.resolve("data.bin");

        int lineCounter = 0;
        int stateCounter = 0;

        try (DataOutputStream dosOffset = IOUtil.getDataOutputStream(offsetPath, 1 << 20);
             DataOutputStream dos = IOUtil.getDataOutputStream(dataPath, 1 << 20);
             BufferedReader reader = Files.newBufferedReader(fstPath, StandardCharsets.UTF_8)) {


            String line;

            int currentState = -1;

            dosOffset.writeInt(0);
            dos.writeInt(0);

            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                String[] tokens = line.split("[\t]");
                int tokenCount = tokens.length;

                FstItem item = new FstItem();
                // there is always a state id.
                int state = Integer.parseInt(tokens[0]);
                if (state != currentState) {
                    dosOffset.writeInt(lineCounter);
                    currentState = state;
                    stateCounter++;
                }
                item.state = state;

                switch (tokenCount) {
                    case 1:
                        // this is the terminal state. we already saved it.
                        break;
                    case 2:
                        // write state with score.
                        item.score = Float.parseFloat(tokens[1]);
                        break;
                    case 4:
                        item.targetState = Integer.parseInt(tokens[1]); // target state
                        item.transitionId = Integer.parseInt(tokens[2]); // transition id
                        item.wordId = Integer.parseInt(tokens[3]); // word id
                        break;
                    case 5:
                        item.targetState = Integer.parseInt(tokens[1]); // target state
                        item.transitionId = Integer.parseInt(tokens[2]); // transition id
                        item.wordId = Integer.parseInt(tokens[3]); // word id
                        item.score = Float.parseFloat(tokens[4]); // score
                        break;
                    default:
                        throw new IllegalStateException("Cannot identify line : " + line);
                }

                dos.writeInt(item.targetState);
                dos.writeInt(item.transitionId);
                dos.writeInt(item.wordId);
                dos.writeFloat(item.score);

                if (lineCounter % 1000000 == 0) {
                    logger.info("Line processed so far = {}", lineCounter);
                }
                lineCounter++;
            }
        }
        try(RandomAccessFile raf = new RandomAccessFile(offsetPath.toFile(), "rw")) {
            raf.writeInt(stateCounter);
        }
        try(RandomAccessFile raf = new RandomAccessFile(dataPath.toFile(), "rw")) {
            raf.writeInt(lineCounter);
        }
    }

    static class FstItem {
        int state;
        int targetState = -1;
        int transitionId = -1;
        int wordId = -1;
        float score = 0;
    }

    public static List<FstItem> loadFromBinary(Path path) throws IOException {
        List<FstItem> items = new ArrayList<>(1000000);
        try (DataInputStream dis = IOUtil.getDataInputStream(path, 1 << 16)) {
            int counter = 0;

            int tokenLength;
            while ((tokenLength = dis.readByte()) > 0) {

                FstItem item = new FstItem();
                item.state = dis.readInt();
                switch (tokenLength) {
                    case 1:
                        break;
                    case 2:
                        item.score = dis.readFloat();
                        break;
                    case 4:
                        item.targetState = dis.readInt();
                        item.transitionId = dis.readInt();
                        item.wordId = dis.readInt();
                        break;
                    case 5:
                        item.targetState = dis.readInt();
                        item.transitionId = dis.readInt();
                        item.wordId = dis.readInt();
                        item.score = dis.readFloat();
                }
                //items.add(item);
                if (counter % 1000000 == 0) {
                    logger.info("Items processed so far = {}", counter);
                }
                counter++;
            }
        }
        return items;
    }

    public static void main(String[] args) throws IOException {
/*        convertBinary(
                Paths.get("../../data/large-16khz/HCLG.fst.txt"),
                Paths.get("../../data/large-16khz/HCLG.fst.bin")
        );*/
        convertRawBinary(
                Paths.get("../../data/large-16khz/HCLG.fst.txt"),
                Paths.get("../../data/large-16khz/fst-bin")
        );
        //loadFromBinary(Paths.get("../../data/large-16khz/HCLG.fst.bin"));
    }

}
