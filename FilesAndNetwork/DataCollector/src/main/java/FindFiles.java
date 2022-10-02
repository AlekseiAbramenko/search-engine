import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindFiles {

    public static List findFiles(Path path, String[] fileExtensions) {
        try {
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException("Path must be a directory!");
            }
            List result;
            try (Stream walk = Files.walk(path, 10)) {
                result = (List) walk
                        .filter(p -> !Files.isDirectory((Path) p))
                        .filter(f -> Arrays.stream(fileExtensions).anyMatch(f.toString()::endsWith))
                        .collect(Collectors.toList());
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
