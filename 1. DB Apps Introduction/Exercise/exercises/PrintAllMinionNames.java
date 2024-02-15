package exercises;

import utils.Constants;
import utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrintAllMinionNames {
    private static final String GET_ALL_MINIONS = "SELECT name FROM minions;";

    public static void main(String[] args) throws SQLException {
        Connection connection = Util.getConnection();
        PreparedStatement statement = connection.prepareStatement(GET_ALL_MINIONS,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);

        ResultSet resultSet = statement.executeQuery();

        int minionsCount = 0;
        while (resultSet.next()) minionsCount++;

        resultSet.beforeFirst();

        int firstIndex = 1;
        int lastIndex = minionsCount;

        for (int i = 1; i < minionsCount + 1; i++) {
            if (i % 2 == 0) {
                resultSet.absolute(firstIndex);
                firstIndex++;
            } else {
                resultSet.absolute(lastIndex);
                lastIndex--;
            }

            System.out.println(resultSet.getString(Constants.COLUMN_NAME));

            resultSet.next();
        }

        connection.close();
    }
}
