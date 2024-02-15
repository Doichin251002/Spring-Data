package exercises;

import utils.Constants;
import utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class GetMinionsNames {
    private static final String GET_MINIONS_NAMES_AND_AGE_QUERY = """
            SELECT m.name, m.age
            FROM minions m JOIN minions_villains mv ON m.id = mv.minion_id
            WHERE mv.villain_id = ?;""";
    private static final String GET_VILLAIN_NAME_QUERY = "SELECT name FROM villains WHERE id = ?;";

    private static final String PRINT_MINION_FORMAT = "%d. %s %d \n";

    private static final String PRINT_VILLAIN_FORMAT = "Villain: %s \n";

    private static final String PRINT_NO_VILLAIN_EXISTS = "No villain with ID %d exists in the database.";

    public static void main(String[] args) throws SQLException {
        int inputVillainID = new Scanner(System.in).nextInt();

        Connection connection = Util.getConnection();

        ResultSet resultForVillain = getResultFromStatement(connection, inputVillainID, GET_VILLAIN_NAME_QUERY);

        if (!resultForVillain.next()) {
            System.out.printf(PRINT_NO_VILLAIN_EXISTS, inputVillainID);
            connection.close();
            return;
        }

        ResultSet resultForMinions = getResultFromStatement(connection, inputVillainID, GET_MINIONS_NAMES_AND_AGE_QUERY);
        print(resultForVillain, resultForMinions);

        connection.close();
    }

    private static void print(ResultSet villainResult, ResultSet minionsResult) throws SQLException {
        String villainName = villainResult.getString(1);
        System.out.printf(PRINT_VILLAIN_FORMAT, villainName);

        for (int i = 1; minionsResult.next(); i++) {
            String name = minionsResult.getString(Constants.COLUMN_NAME);
            int age = minionsResult.getInt(Constants.COLUMN_AGE);
            System.out.printf(PRINT_MINION_FORMAT, i, name, age);
        }
    }

    private static ResultSet getResultFromStatement(Connection connection, int inputValue, String query) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, inputValue);

        return statement.executeQuery();
    }
}
