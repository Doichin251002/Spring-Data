package exercises;

import utils.Constants;
import utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetVillainsNames {
    private static final String GET_VILLAINS_NAMES_QUERY =
            "SELECT v.name, COUNT(DISTINCT mv.minion_id) AS 'count minions' " +
            "FROM villains v " +
            "JOIN minions_villains mv ON v.id = mv.villain_id " +
            "GROUP BY v.id " +
            "HAVING `count minions` > ? " +
            "ORDER BY `count minions` DESC;";

    private static final String PRINT_FORMAT = "%s %d";
    private static final String COLUMN_LABEL_COUNT_MINIONS = "count minions";

    public static void main(String[] args) throws SQLException {
        Connection connection = Util.getConnection();

        PreparedStatement statement = connection.prepareStatement(GET_VILLAINS_NAMES_QUERY);
        statement.setInt(1, 15);

        ResultSet result = statement.executeQuery();
        while (result.next()) {
            print(result);
        }

        connection.close();
    }

    private static void print(ResultSet result) throws SQLException {
        String name = result.getString(Constants.COLUMN_NAME);
        int countMinions = result.getInt(COLUMN_LABEL_COUNT_MINIONS);

        System.out.printf(PRINT_FORMAT, name, countMinions);
    }
}