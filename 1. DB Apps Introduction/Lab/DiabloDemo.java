import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class DiabloDemo {

    public static final String COLUMN_LABEL_USER_NAME = "full_name";
    public static final String COLUMN_LABEL_GAMES_COUNT = "games_count";
    private static final String SELECT_USER_GAMES_COUNT_BY_USERNAME = """
            SELECT CONCAT_WS(' ', first_name, last_name) `full_name`,
                COUNT(game_id) `games_count`
            FROM users_games ug
            JOIN users u on u.id = ug.user_id
            WHERE u.user_name = ?
            GROUP BY u.id;""";

    public static void main(String[] args) throws SQLException {
        Connection connection = getMySQLConnection();

        String inputUsername = readUsername();

        PreparedStatement statement = connection.prepareStatement(SELECT_USER_GAMES_COUNT_BY_USERNAME);
        statement.setString(1, inputUsername);

        ResultSet set = statement.executeQuery();

        printOutput(inputUsername, set);

        connection.close();
    }

    private static void printOutput(String inputUsername, ResultSet set) throws SQLException {
        if (set.next()) {
            System.out.printf("User: %s%n", inputUsername);

            System.out.printf("%s has played %d games",
                    set.getString(COLUMN_LABEL_USER_NAME),
                    set.getInt(COLUMN_LABEL_GAMES_COUNT));
        } else {
            System.out.println("No such user exists");
        }
    }

    private static String readUsername() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private static Connection getMySQLConnection() throws SQLException {
        String databaseURL = "jdbc:mysql://localhost:3306/";
        String databaseName = "diablo";
        databaseURL += databaseName;

        Properties properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "dodi");

        return DriverManager.getConnection(databaseURL, properties);
    }
}
