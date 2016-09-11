package suskun.asr.decoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suskun.core.io.IOUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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


    static class FstItem {
        int state;
        int targetState = -1;
        int transitionId = -1;
        int wordId = -1;
        float score = 0;
    }

    public static List<FstItem> loadFromBinary(Path path) throws IOException {
        List<FstItem> items = new ArrayList<>(1000000);
        try (DataInputStream dis = IOUtil.getDataInputStream(path, 1<<20)) {
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
                items.add(item);
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
        loadFromBinary(Paths.get("../../data/large-16khz/HCLG.fst.bin"));
    }

}
