import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

import static java.nio.file.Paths.get;

public class StationsCount {

    private static final String MAP_JSON = "src/main/resources/map.json";

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
}
