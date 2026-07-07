package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    private static final String DATA_DIR = "data" + File.separator;

    static { new File(DATA_DIR).mkdirs(); }

    public static List<String> readLines(String filename) {
        List<String> lines = new ArrayList<>();
        File file = new File(DATA_DIR + filename);
        if (!file.exists()) return lines;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null)
                if (!line.trim().isEmpty()) lines.add(line.trim());
        } catch (IOException e) {
            System.err.println("Read error [" + filename + "]: " + e.getMessage());
        }
        return lines;
    }

    public static void writeLines(String filename, List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + filename))) {
            for (String line : lines) { bw.write(line); bw.newLine(); }
        } catch (IOException e) {
            System.err.println("Write error [" + filename + "]: " + e.getMessage());
        }
    }
}
