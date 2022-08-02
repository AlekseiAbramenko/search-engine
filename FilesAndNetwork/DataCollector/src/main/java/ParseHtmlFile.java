import java.nio.file.Files;
import java.util.List;

import static java.nio.file.Paths.get;

public class ParseHtmlFile {

    public static String getHtmlFile(String path) {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(get(path));
            lines.forEach(line -> builder.append(line + "\n"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }
}
