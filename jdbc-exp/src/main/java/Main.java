import java.sql.*;

public class Main {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/skillbox";
        String user = "root";
        String pass = "Test123";

        try {
            Connection connection = DriverManager.getConnection(url, user, pass);

            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT course_name, (count(*) / (MONTH(MAX(subscription_date)) - MONTH(MIN(subscription_date)) + 1)) `sells_per_month` FROM PurchaseList group by course_name");
            while (resultSet.next()) {
                String courseName = resultSet.getString("course_name");
                String sellsPerMonth = resultSet.getString("sells_per_month");

                double value = Double.parseDouble(sellsPerMonth);
                String sellsPerMonth1 = String.format("%.2f",value);

                System.out.println(courseName + " " + sellsPerMonth1);
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
