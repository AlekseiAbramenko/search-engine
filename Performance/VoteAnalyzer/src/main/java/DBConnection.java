import java.sql.*;

public class DBConnection {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/learn";
        String user = "root";
        String pass = "testtest";

        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE voter_count(")
                .append("id INT NOT NULL AUTO_INCREMENT, ")
                .append("name TINYTEXT NOT NULL, ")
                .append("birthDate DATE NOT NULL, ")
                .append("`count` INT NOT NULL, ")
                .append("PRIMARY KEY(id))");
        try {
            connection = DriverManager.getConnection(url, user, pass);
            connection.createStatement().execute("DROP TABLE IF EXISTS voter_count");
            connection.createStatement().execute(builder.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection.setAutoCommit(false);
        return connection;
    }
}