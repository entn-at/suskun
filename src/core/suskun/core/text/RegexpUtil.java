package suskun.core.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpUtil {

    public static List<String> getMatchesForGroup(String str, Pattern pattern, int groupIndex) {
        List<String> result = new ArrayList<>();
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            result.add(m.group(groupIndex));
        }
        return result;
    }

    public static List<String> getMatches(String str, Pattern pattern) {
        List<String> result = new ArrayList<>();
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            result.add(m.group());
        }
        return result;
    }

}
