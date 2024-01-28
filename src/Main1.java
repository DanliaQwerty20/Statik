import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Main1 {
    public static void main(String[] args) throws IOException {

        String textFilePath = "src/resources/text.txt";
        String patternsFilePath = "src/resources/patterns.txt";
        String outputFilePath = "src/resources/output.txt";

        String[] text = readFile(textFilePath);
        String[] patterns = readFile(patternsFilePath);

        Map<String, Integer> patternResults = new HashMap<>();

        Arrays.asList(patterns).parallelStream().forEach(pattern -> {
            int count = countMatchingWords(text, pattern);
            patternResults.put(pattern, count);
        });

        writeResultsToFile(outputFilePath, patternResults);
    }

    private static void writeResultsToFile(String outputFilePath, Map<String, Integer> patternResults) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            patternResults.forEach((pattern, count) -> {
                try {
                    writer.write(pattern + "\t" + count + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static String[] readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {

                    content.append(trimmedLine).append("\n");
                }
            }
        }

        return content.toString().split("\\s+");
    }

    private static int countMatchingWords(String[] words, String pattern) {
        AtomicInteger count = new AtomicInteger(0);

        if (pattern.startsWith("\"") && pattern.endsWith("\"")) {
            String sequence = pattern.substring(1, pattern.length() - 1);
            Arrays.asList(words).parallelStream().forEach(word -> {
                if (word.contains(sequence)) {
                    count.incrementAndGet();
                }
            });
        } else {
            Map<Character, Integer> charCount = getCharCount(pattern);

            Arrays.asList(words).parallelStream().forEach(word -> {
                if (matchesPattern(word, charCount)) {
                    count.incrementAndGet();
                }
            });
        }

        return count.get();
    }

    private static Map<Character, Integer> getCharCount(String pattern) {
        Map<Character, Integer> charCount = new HashMap<>();

        char currentChar = '\0';
        int count = 0;
        boolean flag = false;
        for (char c : pattern.toCharArray()) {
            if ((Character.isLetter(c) || ((pattern.charAt(0)) <= '9')) && (!flag)) {
                flag = true;
                currentChar = c;
                count = 0;
            } else if (Character.isDigit(c)) {
                count = count * 10 + Character.getNumericValue(c);
                flag = false;
            }

            if (currentChar != '\0') {
                charCount.put(currentChar, count);
            }
        }

        return charCount;
    }

    private static boolean matchesPattern(String word, Map<Character, Integer> charCount) {
        for (char c : charCount.keySet()) {
            int requiredCount = charCount.get(c);
            long actualCount = word.chars().filter(ch -> ch == c).count();

            if (requiredCount > actualCount) {
                return false;
            }
        }
        return true;
    }
}
