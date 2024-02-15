package exercises;

import utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AddMinion {
    private static final String GET_MINION_QUERY = """
            SELECT m.id FROM minions m
            WHERE m.`name` LIKE ?
            LIMIT 1;""";
    private static final String INSERT_MINION_QUERY = """
             INSERT INTO minions (`name`, age, town_id)
             VALUES (?, ?, ( SELECT id FROM towns WHERE `name` LIKE ?));""";
    private static final String INSERT_MINION_VILLAIN_QUERY = """
             INSERT INTO minions_villains (minion_id, villain_id)
             VALUES (?, ?);""";

    private static final String PRINT_MINION_ADDED_FORMAT = "Successfully added %s to be minion of %s.\n";

    private static final String GET_TOWN_QUERY = "SELECT id FROM towns WHERE `name` LIKE ?;";
    private static final String INSERT_TOWN_QUERY = "INSERT INTO towns(`name`) VALUE (?);";
    private static final String PRINT_TOWN_ADDED_FORMAT = "Town %s was added to the database.\n";

    private static final String GET_VILLAIN_QUERY = "SELECT id FROM villains WHERE `name` LIKE ?;";
    private static final String INSERT_VILLAIN_QUERY =
            "INSERT INTO villains (`name`, evilness_factor) " +
            "VALUES (?, ?);";

    private static final String DEFAULT_EVILNESS_FACTOR = "evil";
    private static final String PRINT_VILLAIN_ADDED_FORMAT = "Villain %s was added to the database.\n";

    private static final String GET_COLUMN_ID = "id";

    public static void main(String[] args) throws SQLException {
        Connection connection = Util.getConnection();

        Scanner scanner = new Scanner(System.in);

        String[] inputDataMinion = scanner.nextLine().split("\\s+");
        String minionName = inputDataMinion[1];
        int minionAge = Integer.parseInt(inputDataMinion[2]);
        String minionTown = inputDataMinion[3];

        String villainName = scanner.nextLine().split("\\s+")[1];

        int townID = getEntryId(connection,
                List.of(minionTown),
                GET_TOWN_QUERY,
                INSERT_TOWN_QUERY,
                PRINT_TOWN_ADDED_FORMAT);

        int villainID = getEntryId(connection,
                List.of(villainName, DEFAULT_EVILNESS_FACTOR),
                GET_VILLAIN_QUERY,
                INSERT_VILLAIN_QUERY,
                PRINT_VILLAIN_ADDED_FORMAT);

        int minionID = getMinionID(connection, minionName, minionAge, townID);

        insertMinionAndVillain(connection, minionName, villainName, villainID, minionID);

        connection.close();

    }

    private static void insertMinionAndVillain(Connection connection, String minionName, String villainName, int villainID, int minionID) throws SQLException {
        PreparedStatement insertStatement = connection.prepareStatement(INSERT_MINION_VILLAIN_QUERY);
        insertStatement.setInt(1, minionID);
        insertStatement.setInt(2, villainID);

        insertStatement.executeUpdate();

        System.out.printf(PRINT_MINION_ADDED_FORMAT, minionName, villainName);
    }

    private static int getMinionID(Connection connection, String minionName, int minionAge, int townID) throws SQLException {
        PreparedStatement insertMinionStatement = connection.prepareStatement(INSERT_MINION_QUERY);
        insertMinionStatement.setString(1, minionName);
        insertMinionStatement.setInt(2, minionAge);
        insertMinionStatement.setInt(3, townID);

        insertMinionStatement.executeUpdate();

        PreparedStatement statement = connection.prepareStatement(GET_MINION_QUERY);
        statement.setString(1, minionName);

        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(GET_COLUMN_ID);
    }

    private static int getEntryId(Connection connection, List<String> args, String getQuery, String insertQuery, String format) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(getQuery);
        String name = args.get(0);
        statement.setString(1, name);

        ResultSet resultSet = statement.executeQuery();

        if (!resultSet.next()) {
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);

            for (int i = 1; i <= args.size(); i++) {
                insertStatement.setString(i, args.get(i - 1));
            }

            insertStatement.executeUpdate();

            ResultSet afterUpdate = statement.executeQuery();
            afterUpdate.next();

            System.out.printf(format, name);

            return afterUpdate.getInt(GET_COLUMN_ID);
        }

        return resultSet.getInt(GET_COLUMN_ID);
    }
}
