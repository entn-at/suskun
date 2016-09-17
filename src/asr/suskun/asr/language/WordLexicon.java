package suskun.asr.language;

import suskun.core.StringPair;
import suskun.core.collections.UIntMap;
import suskun.core.collections.UIntValueMap;
import suskun.core.io.TextUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class WordLexicon {
    UIntValueMap<String> wordMap = new UIntValueMap<>();
    UIntMap<String> indexMap = new UIntMap<>();

    public WordLexicon(UIntValueMap<String> wordMap, UIntMap<String> indexMap) {
        this.wordMap = wordMap;
        this.indexMap = indexMap;
    }

    public static WordLexicon fromTextFileWithIndex(Path path) throws IOException {
        List<String> lines = TextUtil.loadLines(path);
        UIntValueMap<String> map = new UIntValueMap<>(lines.size());
        UIntMap<String> indexMap = new UIntMap<>(lines.size());
        for (String line : lines) {
            StringPair pair = StringPair.fromString(line);
            String word = pair.first;
            int index = Integer.parseInt(pair.second);
            if (map.contains(word)) {
                throw new IllegalArgumentException("Duplicated word in line : [" + line + "]");
            }
            if (indexMap.containsKey(index)) {
                throw new IllegalArgumentException("Duplicated index in line : [" + line + "]");
            }
            if (index < 0) {
                throw new IllegalArgumentException("Index Value cannot be negative : [" + line + "]");
            }
            map.put(word, index);
            indexMap.put(index, word);
        }
        return new WordLexicon(map, indexMap);
    }

    public int getIndex(String s) {
        return wordMap.get(s);
    }

    public String getWord(int index) {
        return indexMap.get(index);
    }

}
