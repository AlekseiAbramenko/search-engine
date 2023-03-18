import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Loader {
    private static XMLHandler handler = new XMLHandler();
    private static HashMap<Voter, Integer> voterCounts = new HashMap<>();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, SQLException {
        long start = System.currentTimeMillis();
        String fileName = "res/data-18M.xml";
        parseFile(fileName);

        System.out.println("parseFile completed in " + (System.currentTimeMillis()-start) + " ms");

        DBConnection.getConnection();
        loadVotersToDB(voterCounts, DBConnection.getConnection());

        System.out.println("program completed in : " + (System.currentTimeMillis() - start) + " ms");
    }

    public static void parseFile(String fileName) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(fileName, handler);
    }

    public static HashMap<Voter, Integer> getVoterCounts() {
        return voterCounts;
    }

    public static void loadVotersToDB(HashMap<Voter, Integer> voterCounts, Connection connection) throws SQLException {
        long start = System.currentTimeMillis();
        int batchSize = 200000;
        int i = 0, j = 0;
        PreparedStatement pstmt =
                connection.prepareStatement("INSERT INTO voter_count(name, birthDate, count) VALUES(?,?,?)");
        for (Map.Entry<Voter, Integer> entry : voterCounts.entrySet()) {
            String name = entry.getKey().getName();
            String birthDay = dateFormat.format(entry.getKey().getBirthDay());
            String count = String.valueOf(entry.getValue());
            pstmt.setString(1, name);
            pstmt.setString(2, birthDay);
            pstmt.setString(3, count);
            pstmt.addBatch();
            i++;
            if (i == batchSize) {
                try {
                    pstmt.executeBatch();
                    connection.commit();
                    i = 0;
                    j++;
                    System.out.println(j + " batch completed " + (System.currentTimeMillis() - start) + " ms");
                } catch (BatchUpdateException e) {
                    e.printStackTrace();
                    connection.rollback();
                }
            }
        }
        pstmt.executeBatch();
        connection.commit();
        System.out.println("last batch completed " +
                (System.currentTimeMillis() - start) + " ms. Consists of " + i + " lines.");
        System.out.println("load file completed in : " + (System.currentTimeMillis() - start) + " ms");
    }
}