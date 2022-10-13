import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseDataFromFiles {

    public static Map<String, String> stationsDates = new HashMap<>();
    public static Map<String, String> stationsDepths = new HashMap<>();

    public static void parseDates(String path) {
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
                    stationsDates.put(fragments[0], fragments[1]);
                }
            } else if (path.endsWith("json")) {
                JSONParser parser = new JSONParser();
                JSONArray stationsArray = (JSONArray) parser.parse(getJsonFile(path));
                stationsArray.forEach(stationObject -> {
                    JSONObject stationJsonObject = (JSONObject) stationObject;
                    stationsDates.put(
                            (String) stationJsonObject.get("name"),
                            (String) stationJsonObject.get("date"));
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void parseDepths(String path) {
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
