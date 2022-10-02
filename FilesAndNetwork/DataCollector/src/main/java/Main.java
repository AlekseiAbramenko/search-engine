import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Main {
    private static final String DATA_PATH = "C:\\Users\\Aleksei\\Downloads\\data\\data";

    public static void main(String[] args) {
//записываем html файл в строку
        String htmlFile = ParseHtmlFile.getHtmlFile("src/doc/input.html");
//обходим папки, достаем пути к нужным файлам
        String[] extensions = {"json", "csv"};
        List files = FindFiles.findFiles(Paths.get(DATA_PATH), extensions);
        Objects.requireNonNull(files).forEach(System.out::println);
//парсим даты
        ParseDataFromFiles.parseDates("C:\\Users\\Aleksei\\Downloads\\data\\data\\4\\6\\dates-1.csv");
        ParseDataFromFiles.parseDates("C:\\Users\\Aleksei\\Downloads\\data\\data\\0\\5\\dates-2.json");
        ParseDataFromFiles.parseDates("C:\\Users\\Aleksei\\Downloads\\data\\data\\9\\6\\dates-3.csv");
//парсим глубину
        ParseDataFromFiles.parseDepths("C:\\Users\\Aleksei\\Downloads\\data\\data\\2\\4\\depths-1.json");
        ParseDataFromFiles.parseDepths("C:\\Users\\Aleksei\\Downloads\\data\\data\\7\\1\\depths-2.csv");
        ParseDataFromFiles.parseDepths("C:\\Users\\Aleksei\\Downloads\\data\\data\\4\\6\\depths-3.json");
//записываем файл map.json
        MakeJsonFiles.linesToPrettyPrinting((MakeJsonFiles.parseMapToJson(htmlFile)), "map.json");
//записываем файл stations.json
        MakeJsonFiles.linesToPrettyPrinting(MakeJsonFiles.parseStationsToJson(htmlFile), "stations.json");
//печатаем список линий + кол-во станций на них
        StationsCount.parseStationsCount();
    }
}
