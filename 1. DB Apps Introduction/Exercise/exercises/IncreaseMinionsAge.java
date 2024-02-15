package exercises;

import utils.Constants;
import utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

public class IncreaseMinionsAge {
    private static final String UPDATE_MINION_NAMES_AND_AGES_QUERY =
            "UPDATE minions SET `name` = LOWER(`name`), age = age + 1 WHERE id = ?;";
    private static final String GET_MINION_NAMES_AND_AGES =
            "SELECT `name`, age FROM minions;";

    private static final String PRINT_MINIONS_FORMAT = "%s %d %n";

    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        int [] inputMinionsIDs = Arrays
                .stream(scanner.nextLine().split("\\s+"))
                .mapToInt(Integer::parseInt)
                .toArray();

        Connection connection = Util.getConnection();

        try {
            connection.setAutoCommit(false);

            updateMinions(inputMinionsIDs, connection);

            print(connection, GET_MINION_NAMES_AND_AGES);
        } catch (SQLException e) {
            e.printStackTrace();

            connection.rollback();
        }
        connection.close();
    }

    private static void updateMinions(int[] inputMinionsIDs, Connection connection) throws SQLException {
        PreparedStatement updateStatement = connection.prepareStatement(UPDATE_MINION_NAMES_AND_AGES_QUERY);

        for (int inputMinionsID : inputMinionsIDs) {
            updateStatement.setInt(1, inputMinionsID);
            updateStatement.executeUpdate();
        }
    }

    private static void print(Connection connection, String query) throws SQLException {
        ResultSet resultSet = getResult(connection, query);

        while (resultSet.next()) {
            String nameMinion = resultSet.getString(Constants.COLUMN_NAME);
            int ageMinion = resultSet.getInt(Constants.COLUMN_AGE);

            System.out.printf(PRINT_MINIONS_FORMAT, nameMinion, ageMinion);
        }
    }

    private static ResultSet getResult(Connection connection, String query) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        return statement.executeQuery();
    }
}
