package suskun.asr.language;

import suskun.core.collections.BidirectionalIndexLookup;

import java.io.IOException;
import java.nio.file.Path;

public class WordLexicon {
    BidirectionalIndexLookup<String> lookup;

    WordLexicon(BidirectionalIndexLookup<String> lookup) {
        this.lookup = lookup;
    }

    public static WordLexicon fromTextFileWithIndex(Path path) throws IOException {
        BidirectionalIndexLookup<String> lookup = BidirectionalIndexLookup.fromTextFileWithIndex(path, ' ');
        return new WordLexicon(lookup);
    }

    public int getIndex(String s) {
        return lookup.getIndex(s);
    }

    public String getWord(int index) {
        return lookup.getKey(index);
    }

}
