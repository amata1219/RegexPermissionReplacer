package amata1219.regex.permission.replacer.regex;

import amata1219.regex.permission.replacer.Pair;

import java.util.ArrayList;
import java.util.List;

public class RegexOperations {

    public static boolean checkSyntax(String regex, String replacement) {
        List<String> patterns = extractPatterns(regex);
        System.out.println("DEBUG: " + patterns.size() + " regex: " + regex + ", rp: " + replacement);
        for (int i = 0; i < patterns.size(); i++) {
            if (!replacement.contains("$" + (i + 1))) return false;
        }
        return true;
    }

    public static Pair<String, String> swap(String regex, String replacement) {
        List<String> patterns = extractPatterns(regex);
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            String placeholder = "$" + (i + 1);
            regex = regex.replace(pattern, placeholder);
            replacement = replacement.replace(placeholder, pattern);
        }
        return new Pair<>(regex, replacement);
    }

    private static List<String> extractPatterns(String regex) {
        List<String> patterns = new ArrayList<>();

        int bracketsCount = 0, start = 0;
        boolean isEscapeSequence = false;
        for (int i = 0; i < regex.length(); i++) {
            if (isEscapeSequence) {
                isEscapeSequence = false;
                continue;
            }

            switch (regex.charAt(i)) {
                case '(':
                    if (bracketsCount == 0) start = i;
                    bracketsCount++;
                    continue;
                case ')':
                    bracketsCount--;
                    if (bracketsCount == 0) {
                        String pattern = regex.substring(start, i + 1);
                        patterns.add(pattern);
                    }
                    continue;
                case '\\':
                    isEscapeSequence = true;
                default:
                    continue;
            }
        }

        return patterns;
    }

}
