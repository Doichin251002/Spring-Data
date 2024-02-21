package orm;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityManager<E> implements DBContext<E> {
    private static final String CREATE_TABLE_QUERY_FORMAT = "CREATE TABLE IF NOT EXISTS %s (id INT PRIMARY KEY AUTO_INCREMENT, %s);";
    private static final String SELECT_COLUMN_NAMES_QUERY_FORMAT = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'soft_uni' AND COLUMN_NAME != 'id' AND TABLE_NAME = '%s';";
    private static final String ALTER_TABLE_QUERY_FORMAT = "ALTER TABLE %s %s;";
    private static final String ADD_COLUMN_FORMAT = "ADD COLUMN %s %s;";
    private static final String INSERT_QUERY_FORMAT = "INSERT INTO %s(%s) VALUES (%s);";
    private static final String UPDATE_QUERY_FORMAT = "UPDATE %s SET %s WHERE id = %d;";
    private static final String SELECT_QUERY_FORMAT = "SELECT * FROM %s %s;";
    private static final String DELETE_QUERY_FORMAT = "DELETE FROM %s WHERE id = %s;";
    private Connection connection;

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean persist(E entity) throws IllegalAccessException, SQLException {
        Field idField = getFieldId(entity.getClass());
        idField.setAccessible(true);
        Object idValue = idField.get(entity);

        if (idValue == null || (int) idValue == 0) {
            return insertEntity(entity);
        }

        return updateEntity(entity, (int) idValue);
    }

    @Override
    public Iterable<E> find(Class<E> table) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return find(table, null);
    }

    @Override
    public Iterable<E> find(Class<E> table, String where) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String actualWhere = where == null ? "" : "WHERE " + where;
        String tableName = getTableName(table);

        String query = String.format(SELECT_QUERY_FORMAT, tableName, actualWhere);
        PreparedStatement statement = this.connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        List<E> entities = new ArrayList<>();
        while (resultSet.next()) {
            E entity = createEntity(table, resultSet);
            entities.add(entity);
        }

        return entities;
    }

    @Override
    public E findFirst(Class<E> table) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return findFirst(table, null);
    }

    @Override
    public E findFirst(Class<E> table, String where) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return find(table, where).iterator().next();
    }

    @Override
    public void createTable(Class<E> entityClass) throws SQLException {
        String tableName = getTableName(entityClass);

        String fieldsData = getFieldsTypeData(entityClass);

        String query = String.format(CREATE_TABLE_QUERY_FORMAT, tableName, fieldsData);
        PreparedStatement statement = this.connection.prepareStatement(query);
        statement.execute();
    }

    @Override
    public void alterTable(Class<E> entityClass) throws SQLException {
        String tableName = getTableName(entityClass);

        String addColumnsStatement = addNewColumnsStatement(entityClass, tableName);

        String query = String.format(ALTER_TABLE_QUERY_FORMAT, tableName, addColumnsStatement);
        PreparedStatement statement = this.connection.prepareStatement(query);

        statement.executeUpdate();
    }

    @Override
    public void deleteFirst(E entity) throws SQLException, IllegalAccessException {
        String tableName = getTableName(entity.getClass());

        Field fieldId = getFieldId(entity.getClass());
        fieldId.setAccessible(true);
        String id = fieldId.get(entity).toString();

        String query = String.format(DELETE_QUERY_FORMAT, tableName, id);
        PreparedStatement statement = this.connection.prepareStatement(query);

        statement.executeUpdate();

    }

    private boolean insertEntity(E entity) throws SQLException {
        String tableName = getTableName(entity.getClass());

        String fieldNames = getColumnNames(entity.getClass());
        String fieldValues = getFieldValues(entity);

        String query = String.format(INSERT_QUERY_FORMAT, tableName, fieldNames, fieldValues);
        PreparedStatement statement = this.connection.prepareStatement(query);

        return statement.executeUpdate() == 1;
    }

    private boolean updateEntity(E entity, int id) throws SQLException {
        String tableName = getTableName(entity.getClass());

        String setValues = getUpdatingValues(entity);
        String query = String.format(UPDATE_QUERY_FORMAT, tableName, setValues, id);
        PreparedStatement statement = this.connection.prepareStatement(query);

        return statement.executeUpdate() == 1;
    }

    private E createEntity(Class<E> table, ResultSet resultSet) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        E entity = table.getDeclaredConstructor().newInstance();

        Arrays.stream(table.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class))
                .forEach(f -> {
                    try {
                        fillFieldData(entity, f, resultSet);
                    } catch (SQLException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

        return entity;
    }

    private void fillFieldData(E entity, Field field, ResultSet resultSet) throws SQLException, IllegalAccessException {
        field.setAccessible(true);

        String fieldName = getColumnName(field);
        Class<?> fieldType = field.getType();

        Object value;
        if (fieldType == int.class) {
            value = resultSet.getInt(fieldName);
        } else if (fieldType == LocalDate.class) {
            String dateString = resultSet.getString(fieldName);
            value = LocalDate.parse(dateString);
        } else {
            value = resultSet.getString(fieldName);
        }

        field.set(entity, value);
    }

    private String addNewColumnsStatement(Class<E> entityClass, String tableName) throws SQLException {
        List<String> sqlColumnNames = getSQLColumns(tableName);
        Stream<Field> entityColumnNames = getFieldStreamWithoutId(entityClass);

        List<String> nonMatchingFields = new ArrayList<>();
        entityColumnNames.forEach(field -> {
            String columnName = getColumnName(field);

            if (!sqlColumnNames.contains(columnName)) {
                String columnType = getSQLType(field.getType());

                String newColumn = String.format(ADD_COLUMN_FORMAT, columnName, columnType);
                nonMatchingFields.add(newColumn);
            }
        });

        return String.join(", ", nonMatchingFields);
    }

    private List<String> getSQLColumns(String tableName) throws SQLException {
        List<String> allFields = new ArrayList<>();

        String query = String.format(SELECT_COLUMN_NAMES_QUERY_FORMAT, tableName);
        PreparedStatement statement = this.connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            allFields.add(resultSet.getString(1));
        }

        return allFields;
    }

    private String getFieldsTypeData(Class<?> entityClass) {
        return getFieldStreamWithoutId(entityClass)
                .map(f -> getColumnName(f) + " " + getSQLType(f.getType()))
                .collect(Collectors.joining(", "));

    }

    private static String getColumnName(Field field) {
        return field.getAnnotation(Column.class).name();
    }

    private static String getSQLType(Class<?> type) {
        if (type == int.class || type == long.class) {
            return "INT";
        } else if (type == LocalDate.class) {
            return "DATE";
        } else {
            return "VARCHAR(20)";
        }
    }

    private static Field getFieldId(Class<?> entity) {
        return Arrays.stream(entity.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(UnsupportedOperationException::new);
    }

    private String getColumnNames(Class<?> entityClass) {
        Stream<Field> fieldsStream = getFieldStreamWithoutId(entityClass);

        return fieldsStream
                .map(EntityManager::getColumnName)
                .collect(Collectors.joining(", "));
    }

    private String getFieldValues(E entity) {
        Stream<Field> fieldsStream = getFieldStreamWithoutId(entity.getClass());

        return fieldsStream
                .map(f -> {
                    f.setAccessible(true);

                    try {
                        Object value = f.get(entity);
                        return String.format("'%s'", value.toString());

                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.joining(", "));
    }

    private String getUpdatingValues(E entity) {
        String[] fieldNames = getColumnNames(entity.getClass()).split(", ");
        String[] fieldValues = getFieldValues(entity).split(", ");

        StringBuilder setValuesBuilder = new StringBuilder();
        for (int i = 0; i < fieldNames.length; i++) {
            setValuesBuilder.append(fieldNames[i])
                    .append(" = ")
                    .append(fieldValues[i]);

            if (i + 1 < fieldNames.length) {
                setValuesBuilder.append(", ");
            }
        }


        return setValuesBuilder.toString().trim();
    }

    private Stream<Field> getFieldStreamWithoutId(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> !(f.isAnnotationPresent(Id.class)))
                .filter(f -> f.isAnnotationPresent(Column.class));
    }

    private String getTableName(Class<?> entity) {
        Entity annotation = entity.getAnnotation(Entity.class);

        if (annotation == null) {
            throw new UnsupportedOperationException("Table must have Entity annotation");
        }

        return annotation.name();
    }
}
