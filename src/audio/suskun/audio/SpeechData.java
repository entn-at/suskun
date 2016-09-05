package suskun.audio;

import com.google.common.base.Splitter;
import suskun.core.FloatData;
import suskun.core.io.TextUtil;
import suskun.core.text.RegexpUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Contains actual speech sound content or features.
 */
public class SpeechData {

    SpeechSegment segment;

    List<FloatData> content = new ArrayList<>();

    public SpeechData(SpeechSegment segment, List<FloatData> content) {
        this.segment = segment;
        this.content = content;
    }

    public SpeechData(SpeechSegment segment, FloatData... content) {
        this.segment = segment;
        this.content = Arrays.asList(content);
    }

    public int vectorCount() {
        return content.size();
    }

    public FloatData get(int i) {
        return content.get(i);
    }

    public List<FloatData> getContent() {
        return content;
    }

    /**
     * Loads from text file with format:
     * <p>
     * id1 [
     * 0.0 0.1 ...
     * 1.0 1.1 ...
     * ... ]
     * id2 [
     * 0.0 0.1 ...
     * 1.0 1.1 ...
     * ... ]
     *
     * @param textFilePath file path.
     * @return a list of speech data.
     */
    public static List<SpeechData> loadFromKaldiTxt(Path textFilePath) throws IOException {
        Pattern idPattern = Pattern.compile("(.+?)(?:\\[.+?\\])", Pattern.DOTALL | Pattern.MULTILINE);
        Pattern dataPattern = Pattern.compile("(?:\\[)(.+?)(?:\\])", Pattern.DOTALL | Pattern.MULTILINE);
        String all = TextUtil.loadUtfAsString(textFilePath);
        List<String> dataBlocks = RegexpUtil.getMatchesForGroup(all, dataPattern, 1);
        List<String> ids = RegexpUtil.getMatchesForGroup(all, idPattern, 1);
        List<SpeechData> result = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i).trim();
            String dataBlock = dataBlocks.get(i);
            // split from new lines.
            List<FloatData> dataForId = new ArrayList<>();
            int lineCounter = 0;
            for (String line : Splitter.on("\n").omitEmptyStrings().trimResults().split(dataBlock)) {
                dataForId.add(FloatData.fromString(lineCounter, line, " "));
                lineCounter++;
            }
            result.add(new SpeechData(SpeechSegment.unknown(id), dataForId));
        }

        return result;
    }

}
