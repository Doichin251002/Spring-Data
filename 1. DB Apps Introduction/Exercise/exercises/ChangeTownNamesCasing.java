package exercises;

import utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ChangeTownNamesCasing {
    private static final String UPDATE_TOWNS_QUERY = "UPDATE towns SET `name` = upper(name) WHERE country LIKE ?;";
    private static final String GET_COUNT_AND_TOWNS_NAMES_QUERY =
            "SELECT count(*), GROUP_CONCAT(name SEPARATOR ', ') " +
                    "FROM towns " +
                    "WHERE country LIKE ? " +
                    "GROUP BY country;";
    private static final String PRINT_NO_TOWN_AFFECTED = "No town names were affected.";
    private static final String PRINT_COUNT_TOWNS_FORMAT = "%d town names were affected. \n";
    private static final String PRINT_TOWNS_NAMES_FORMAT = "[%s]";

    public static void main(String[] args) throws SQLException {
        String inputCountry = new Scanner(System.in).nextLine();

        Connection connection = Util.getConnection();
        PreparedStatement updateStatement = getStatement(connection, UPDATE_TOWNS_QUERY, inputCountry);

        updateStatement.executeUpdate();

        PreparedStatement statement = getStatement(connection, GET_COUNT_AND_TOWNS_NAMES_QUERY, inputCountry);

        ResultSet resultSet = statement.executeQuery();

        if (!resultSet.next()) {
            System.out.print(PRINT_NO_TOWN_AFFECTED);
            return;
        }

        print(resultSet);

        connection.close();
    }

    private static void print(ResultSet resultSet) throws SQLException {
        int count = resultSet.getInt(1);
        String townNames = resultSet.getString(2);

        System.out.printf(PRINT_COUNT_TOWNS_FORMAT, count);
        System.out.printf(PRINT_TOWNS_NAMES_FORMAT, townNames);
    }

    private static PreparedStatement getStatement(Connection connection, String query, String country) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, country);
        return statement;
    }
}
