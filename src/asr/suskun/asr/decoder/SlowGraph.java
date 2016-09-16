package suskun.asr.decoder;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suskun.core.io.IOUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Used for fast loading very large fsts. Should not be used in actual system.
public class SlowGraph {

    public static final int TRANSITION_BYTE_LENGTH = 16;
    public static final int TRANSITION_BLOCK_SIZE = 1_000_000;

    int[] offsets;
    byte[][] data;
    int[] blockOffsets;
    int totalLineCount;

    static Logger logger = LoggerFactory.getLogger(SlowGraph.class);

    public SlowGraph(int[] offsets, byte[][] data, int[] blockOffsets, int totalLineCount) {
        this.offsets = offsets;
        this.data = data;
        this.blockOffsets = blockOffsets;
        this.totalLineCount = totalLineCount;
    }

    public List<Transition> getTransitions(int state) {
        int blockIndex = state / TRANSITION_BLOCK_SIZE;
        int byteIndex = (offsets[state] - blockOffsets[blockIndex]) * 16;
        int lineCount = state == offsets.length - 1 ? totalLineCount - offsets[state] : offsets[state + 1] - offsets[state];
        List<Transition> result = new ArrayList<>(lineCount);
        byte[] block = data[blockIndex];
        for (int i = 0; i < lineCount; i++) {
            int k = byteIndex + i * TRANSITION_BYTE_LENGTH;
            result.add(new Transition(
                    state,
                    loadIntBe(block, k),
                    loadIntBe(block, k + 4),
                    loadIntBe(block, k + 8),
                    Float.intBitsToFloat(loadIntBe(block, k + 12)
                    )));
        }
        return result;
    }


    private int loadIntBe(byte[] ar, int i) {
        return (ar[i] & 0xff) << 24 | (ar[i + 1] & 0xff) << 16 | (ar[i + 2] & 0xff) << 8 | (ar[i + 3] & 0xff);
    }


    public static class Transition {
        int state;
        int tagetState = -1;
        int transitionId = -1;
        int wordId = -1;
        float score = 0;

        public Transition(int state, int tagetState, int transitionId, int wordId, float score) {
            this.state = state;
            this.tagetState = tagetState;
            this.transitionId = transitionId;
            this.wordId = wordId;
            this.score = score;
        }
    }

    public static SlowGraph loadFromDirectory(Path dir) throws IOException {
        Path offsetPath = dir.resolve("offsets.bin");
        Path dataPath = dir.resolve("data.bin");

        int[] offsets;
        try (DataInputStream dis = IOUtil.getDataInputStream(offsetPath, 1 << 20)) {
            int amount = dis.readInt();
            offsets = new int[amount];
            for (int i = 0; i < amount; i++) {
                offsets[i] = dis.readInt();
            }
        }
        logger.info("Offsets loaded.");

        byte[][] data;
        int[] blockOffsets;

        int totalLineCount;
        try (DataInputStream dis = IOUtil.getDataInputStream(dataPath, 1 << 20)) {

            totalLineCount = dis.readInt(); // transition count
            data = new byte[offsets.length / TRANSITION_BLOCK_SIZE + 1][];
            blockOffsets = new int[data.length];

            int blockCounter = 0;
            int transitionCounter = 0;
            int megaBlockCounter = 0;
            for (int i = 1; i < offsets.length; i++) {
                int transitionCount = offsets[i] - offsets[i - 1];
                blockCounter += transitionCount;
                transitionCounter+= transitionCount;
                if (i != 0 && i % TRANSITION_BLOCK_SIZE == 0) {
                    byte[] megaBlock = new byte[blockCounter * TRANSITION_BYTE_LENGTH];
                    dis.readFully(megaBlock);
                    data[megaBlockCounter] = megaBlock;
                    megaBlockCounter++;
                    blockOffsets[megaBlockCounter] = transitionCounter;
                    blockCounter = 0;
                }
            }
            if (blockCounter != 0) {
                byte[] megaBlock = new byte[(blockCounter + (totalLineCount-transitionCounter)) * TRANSITION_BYTE_LENGTH];
                dis.readFully(megaBlock);
                data[megaBlockCounter] = megaBlock;
            }
        }

        logger.info("Done.");
        return new SlowGraph(offsets, data, blockOffsets, totalLineCount);
    }

    public static void main(String[] args) throws IOException {
        SlowGraph slow = loadFromDirectory(Paths.get("../../data/large-16khz/fst-bin"));
        slow.getTransitions(60669929);
    }


}
