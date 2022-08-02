import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Paths.get;

public class MakeJsonFiles {

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
                ParseDataFromFiles.stationsDates.forEach((key1, value1) -> {
                    if(key1.equals(stationName) && value1 != null) {
                        station.put("date", value1);
                    }
                });
                ParseDataFromFiles.stationsDepths.forEach((key2, value2) -> {
                    if(key2.equals(stationName) && value2 != null && !value2.equals("?")) {
                        station.put("depth", value2);
                    }
                });
                stationsArray.add(station);
            });
        });
        JSONObject stations = new JSONObject();
        stations.put("Stations", stationsArray);
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
}
