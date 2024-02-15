package exercises;

import utils.Constants;
import utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class IncreaseAgeStoredProcedure {
    private static final String UPDATE_AGE_PROCEDURE = """
            CREATE PROCEDURE usp_get_older(minion_id INT) BEGIN
                UPDATE minions SET age = age + 1
                WHERE id = minion_id;
            END;""";

    private static final String CALL_PROCEDURE = "CALL usp_get_older(?);";
    private static final String GET_MINION_NAME_AND_AGE =
            "SELECT `name`, age FROM minions WHERE id = ?;";
    private static final String PRINT_MINION_FORMAT = "%s %d";

    public static void main(String[] args) throws SQLException {
        int inputMinionID = new Scanner(System.in).nextInt();

        Connection connection = Util.getConnection();

        try {
            connection.setAutoCommit(false);
            updateMinionAge(inputMinionID, connection);

            PreparedStatement statement = getStatement(inputMinionID, GET_MINION_NAME_AND_AGE, connection);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();

            print(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();

            connection.rollback();
        }
        connection.close();
    }

    private static void updateMinionAge(int inputID, Connection connection) throws SQLException {
        PreparedStatement updateStatement = connection.prepareStatement(UPDATE_AGE_PROCEDURE);
        updateStatement.executeUpdate();

        PreparedStatement callStatement = getStatement(inputID, CALL_PROCEDURE, connection);
        callStatement.executeUpdate();
    }

    private static void print(ResultSet resultSet) throws SQLException {
        String nameMinion = resultSet.getString(Constants.COLUMN_NAME);
        int ageMinion = resultSet.getInt(Constants.COLUMN_AGE);
        System.out.printf(PRINT_MINION_FORMAT, nameMinion, ageMinion);
    }

    private static PreparedStatement getStatement(int minionID, String query, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, minionID);
        return statement;
    }
}
