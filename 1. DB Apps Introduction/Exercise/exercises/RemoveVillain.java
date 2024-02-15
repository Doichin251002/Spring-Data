package exercises;

import utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class RemoveVillain {
    private static final String GET_VILLAIN_QUERY = "SELECT `name` FROM villains WHERE id = ?";
    private static final String GET_COUNT_MINIONS_OF_VILLAIN_QUERY = """
            SELECT COUNT(minion_id) 
            FROM minions_villains 
            WHERE villain_id = ? 
            GROUP BY villain_id;""";
    private static final String DELETE_VILLAIN_ID_QUERY =
            "DELETE FROM minions_villains WHERE villain_id = ?;";

    private static final String DELETE_VILLAIN_QUERY =
            "DELETE FROM villains WHERE id = ?";

    private static final String PRINT_VILLAIN_FORMAT =
            "%s was deleted";

    private static final String PRINT_COUNT_MINIONS_FORMAT = "%d minions released";
    private static final String PRINT_NO_SUCH_VILLAIN_FOUND = "No such villain was found";

    public static void main(String[] args) throws SQLException {
        int inputID = new Scanner(System.in).nextInt();

        Connection connection = Util.getConnection();
        PreparedStatement getVillainStatement = getStatement(inputID, GET_VILLAIN_QUERY, connection);
        ResultSet villainResult = getVillainStatement.executeQuery();

        if (!villainResult.next()) {
            System.out.printf(PRINT_NO_SUCH_VILLAIN_FOUND);
            connection.close();
            return;
        }

        String villainName = villainResult.getString(1);
        int countMinions = getCountMinions(inputID, connection);

        try {
            connection.setAutoCommit(false);
            deleteVillain(inputID, connection);

            connection.commit();

            System.out.printf(PRINT_VILLAIN_FORMAT, villainName);
            System.out.printf(PRINT_COUNT_MINIONS_FORMAT, countMinions);
        } catch (SQLException e) {
            e.printStackTrace();

            connection.rollback();
        }

        connection.close();
    }

    private static void deleteVillain(int inputID, Connection connection) throws SQLException {
        PreparedStatement releaseMinionsStatement = getStatement(inputID, DELETE_VILLAIN_ID_QUERY, connection);
        releaseMinionsStatement.executeUpdate();

        PreparedStatement deleteVillainStatement = getStatement(inputID, DELETE_VILLAIN_QUERY, connection);
        deleteVillainStatement.executeUpdate();
    }
    private static int getCountMinions(int inputID, Connection connection) throws SQLException {
        PreparedStatement statement = getStatement(inputID, GET_COUNT_MINIONS_OF_VILLAIN_QUERY, connection);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1);
    }

    private static PreparedStatement getStatement(int inputID, String query, Connection connection) throws SQLException {
        PreparedStatement existentVillainStatement = connection.prepareStatement(query);
        existentVillainStatement.setInt(1, inputID);
        return existentVillainStatement;
    }
}
