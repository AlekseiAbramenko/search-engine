import com.google.gson.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Paths.get;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final String MAP_JSON = "src/main/resources/map.json";
    private static final String DATA_PATH = "C:\\Users\\Aleksei\\Downloads\\data\\data";

    public static Map<String, String> stationsDate = new HashMap<>();
    public static Map<String, String> stationsDepths = new HashMap<>();

    public static void main(String[] args) {
        String htmlFile = getHtmlFile("src/doc/input.html");//записываем html файл в строку
//обходим папки, достаем пути к нужным файлам
        String[] extensions = {"json", "csv"};
        List files = findFiles(Paths.get(DATA_PATH), extensions);
        Objects.requireNonNull(files).forEach(System.out::println);
//парсим даты
        parseStationsDatesFromFiles("C:\\Users\\Aleksei\\Downloads\\data\\data\\4\\6\\dates-1.csv");
        parseStationsDatesFromFiles("C:\\Users\\Aleksei\\Downloads\\data\\data\\0\\5\\dates-2.json");
        parseStationsDatesFromFiles("C:\\Users\\Aleksei\\Downloads\\data\\data\\9\\6\\dates-3.csv");
//парсим глубину
        parseStationsDepthsFromFiles("C:\\Users\\Aleksei\\Downloads\\data\\data\\2\\4\\depths-1.json");
        parseStationsDepthsFromFiles("C:\\Users\\Aleksei\\Downloads\\data\\data\\7\\1\\depths-2.csv");
        parseStationsDepthsFromFiles("C:\\Users\\Aleksei\\Downloads\\data\\data\\4\\6\\depths-3.json");

        linesToPrettyPrinting((parseMapToJson(htmlFile)), "map.json"); //записываем файл map.json
        linesToPrettyPrinting(parseStationsToJson(htmlFile), "stations.json"); //записываем файл stations.json

        parseStationsCount(); //список линий + кол-во станций на них
    }

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

    public static void parseStationsCount() {
        try {
            Reader reader = Files.newBufferedReader(get(MAP_JSON));
            JSONParser parser = new JSONParser();

            JSONObject jsonData = (JSONObject) parser.parse(reader);
            JSONObject stationsObject = (JSONObject) jsonData.get("Stations");
            stationsObject.keySet().forEach(lineNumberObject ->
            {
                String lineNumber = ((String) lineNumberObject);
                JSONArray stationsArray = (JSONArray) stationsObject.get(lineNumberObject);
                System.out.println("Линия: " + lineNumber + " количество станций: " + stationsArray.size());
            });
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String parseStationsToJson(String htmlFile) {
        Document doc = Jsoup.parse(htmlFile);
        Elements pl = doc.select("span[data-line]");
//создаем Map номер линии - название линии
        Map<String, String> lines = new HashMap<>();
        pl.forEach(element -> lines.put((element.attr("data-line")), element.text()));
//создаем и заполняем Json массив из объектов Station
        JSONArray stationsArray = new JSONArray();
        lines.forEach((key, value) -> {
            Elements ps = doc.select("[data-line=\" " + key + " \"] > p");
                ps.forEach(element -> {
                    String stationName = element.text().replaceAll("[0-9.]", "").trim();
                    JSONObject station = new JSONObject();
                    station.put("name", stationName);
                    station.put("line", value);
                    Elements pc = element.select("[title]");
                    pc.forEach(q -> {
                        if(q.attr("title").contains("переход")) {
                            boolean connection = true;
                            station.put("connection", String.valueOf(connection));
                        }
                    });
                    stationsDate.forEach((key1, value1) -> {
                        if(key1.equals(stationName) && value1 != null) {
                            station.put("date", value1);
                        }
                    });
                    stationsDepths.forEach((key2, value2) -> {
                        if(key2.equals(stationName) && value2 != null && !value2.equals("?")) {
                                station.put("depth", value2);
                        }
                    });
                    stationsArray.add(station);
            });
        });
        JSONObject stations = new JSONObject();
        stations.put("Stations", stationsArray);
//        System.out.println(new Gson().toJson(stations1)); //проверка
        return new Gson().toJson(stations); //возвращаем Json файл как строку для последующей записи в файл в удобном формате
    }

    public static String parseMapToJson(String htmlFile) {
//парсим линии и записываем в JSONObject
        Document doc = Jsoup.parse(htmlFile);
        Elements elements = doc.select("span[data-line]");
        JSONArray lineArray = new JSONArray();

        elements.forEach(element -> {
            String lineNumber = element.attr("data-line");
            String lineName = element.text();
            JSONObject line = new JSONObject();
            line.put("number", lineNumber);
            line.put("name", lineName);
            lineArray.add(line);
        });
//парсим станции и записываем в JSONObject
        Elements pl = doc.select("div[data-line]");
        JSONObject stations = new JSONObject();

        List<String> lineNumbers = new ArrayList<>();
        pl.forEach(element -> lineNumbers.add(element.attr("data-line")));
        lineNumbers.forEach(lineNum ->  {
            Elements ps = doc.select("[data-line=\" " + lineNum + " \"] > p");
            JSONArray stationsArray = new JSONArray();
            ps.forEach(element -> {
                String stationName = element.text().replaceAll("[0-9.]", "").trim();
                stationsArray.add(stationName);
            });
            stations.put(lineNum, stationsArray);
        });
//записываем линии и станции в общую карту
        JSONObject map = new JSONObject();
        map.put("Stations", stations);
        map.put("lines", lineArray);

        return new Gson().toJson(map);  //возвращаем карту как строку для последующей записи в файл в удобном формате
    }

    public static void linesToPrettyPrinting(String uglyJSONString, String file) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JSONParser jp = new JSONParser();
        try {
            JSONObject je = (JSONObject) jp.parse(uglyJSONString);
            Writer writer = Files.newBufferedWriter(get(file));
            gson.toJson(je, writer);
            writer.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

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

    public static void parseStationsDatesFromFiles(String path) {
        try {
            if (path.endsWith("csv")) {
                List<String> lines = new BufferedReader(new FileReader(path))
                        .lines()
                        .skip(1).toList();
                for (String line : lines) {
                    String[] fragments = line.split(",");
                    if (fragments.length != 2) {
                        System.out.println("Wrong line: " + line);
                        continue;
                    }
                    stationsDate.put(fragments[0], fragments[1]);
                }
            } else if (path.endsWith("json")) {
                JSONParser parser = new JSONParser();
                JSONArray stationsArray = (JSONArray) parser.parse(getJsonFile(path));
                stationsArray.forEach(stationObject -> {
                    JSONObject stationJsonObject = (JSONObject) stationObject;
                    stationsDate.put(
                            (String) stationJsonObject.get("name"),
                            (String)stationJsonObject.get("date"));
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void parseStationsDepthsFromFiles(String path) {
        try {
            if (path.endsWith("csv")) {
                List<String> lines = new BufferedReader(new FileReader(path))
                        .lines()
                        .skip(1).toList();
                for (String line : lines) {
                    String[] fragments = line.split(",");
                    if (fragments.length != 2) {
                        String str = line.replaceFirst(",", "");
                        String[] fragments1 = str.split("\"");
                        stationsDepths.put(fragments1[0], fragments1[1]);
                        continue;
                    }
                    stationsDepths.put(fragments[0], fragments[1]);
                }
            } else if (path.endsWith("json")) {
                JSONParser parser = new JSONParser();
                JSONArray stationsArray = (JSONArray) parser.parse(getJsonFile(path));
                stationsArray.forEach(stationObject -> {
                    JSONObject stationJsonObject = (JSONObject) stationObject;
                    if(stationJsonObject.containsKey("name")) {
                        stationsDepths.put(
                                (String) stationJsonObject.get("name"),
                                String.valueOf(stationJsonObject.get("depth"))
                        );
                    } else if(stationJsonObject.containsKey("station_name")) {
                        stationsDepths.put(
                                (String) stationJsonObject.get("station_name"),
                                String.valueOf(stationJsonObject.get("depth_meters"))
                        );
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String getJsonFile(String path) {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            lines.forEach(line -> builder.append(line));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }
}
