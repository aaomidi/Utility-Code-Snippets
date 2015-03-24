import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;


public class MySQL {
    private final Logger logger;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String database;
    private final int minPoolSize;
    private final int maxPoolSize;
    private HikariDataSource hikariDataSource;
    private Connection connection;

    /**
     * Create a Database Connection.
     *
     * @param logger Logger instance to use to log important messages.
     * @param host Host of the database.
     * @param port Port of the database.
     * @param username Username of the connection to the database.
     * @param password Password of the aforementioned username.
     * @param database Name of the database.
     * @param minPoolSize Minimum connection pool size.
     * @param maxPoolSize Maximum connection pool size.
     */
    public MySQL(Logger logger, String host, int port, String username, String password, String database, int minPoolSize, int maxPoolSize) {
        this.logger = logger;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.createHikariDataSource();
    }

    private void createHikariDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", this.host, this.port, this.database));
            config.setUsername(this.username);
            config.setPassword(this.password);
            hikariDataSource = new HikariDataSource(config);
            this.createConnection();
        } catch (Exception ex) {
            throw new Error("Unrecoverable error when creating the HikariDataSource object.", ex);
        }
    }

    private void createConnection() {
        try {
            connection = hikariDataSource.getConnection();
        } catch (Exception ex) {
            throw new Error("Unrecoverable error when creating the connection.", ex);
        }
    }

    public boolean isConnected() throws SQLException {
        return (this.connection != null && !this.connection.isClosed());

    }

    protected Connection getConnection() throws SQLException {
        if (!isConnected()) {
            this.createConnection();
        }
        return this.connection;
    }


    /**
     * Execute a query to the specified connection.
     *
     * @param query      Query to execute. Example: SELECT * FROM `exampleTable` WHERE `someColumn`=?;
     * @param parameters Parameters to fill in the question marks with.
     * @return ResultSet returned by SQL. This value CAN be null.
     * @throws java.sql.SQLException
     */
    public ResultSet executeQuery(String query, Object... parameters) {
        int parameterCount = (parameters == null) ? 0 : parameters.length;

        if (StringUtils.countMatches(query, "?") != parameterCount) {
            logger.log(Level.SEVERE, "The number of ? did not match the number of parameters.");
            return null;
        }
        try {
            PreparedStatement statement = prepareStatement(query, parameterCount, parameters);
            return statement.executeQuery();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, String.format("There was a problem executing the following query: %s \n Stack Trace: %s", query, ex));
        }
        return null;
    }

    /**
     * Execute an update to the specified connection.
     *
     * @param query      Query to execute. Example: UPDATE `exampleTable` SET `someColumn` = ? WHERE `otherColumn` = ?;
     * @param parameters Parameters to fill in the question marks with.
     * @return Result integer returned by SQL. If this value is -1 there was a mistake in the number of `?` and parameters.
     * @throws java.sql.SQLException
     */
    public int executeUpdate(String query, Object... parameters) {
        int parameterCount = (parameters == null) ? 0 : parameters.length;

        if (StringUtils.countMatches(query, "?") != parameterCount) {
            logger.log(Level.SEVERE, "The number of ? did not match the number of parameters.");
            return -1;
        }
        try {
            PreparedStatement statement = prepareStatement(query, parameterCount, parameters);
            return statement.executeUpdate();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, String.format("There was a problem executing the following query: %s \n Stack Trace: %s", query, ex));
        }
        return -1;
    }

    /**
     * Prepare a statement by replacing ? with parameters.
     *
     * @param query
     * @param parameterCount
     * @param parameters
     * @return
     * @throws java.sql.SQLException
     */
    protected PreparedStatement prepareStatement(String query, int parameterCount, Object... parameters) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(query);
        Object parameter;

        for (int i = 0, j = 1; i < parameterCount; i++, j++) {
            parameter = parameters[i];
            if (parameter instanceof String) {
                statement.setString(j, (String) parameter);
            } else if (parameter instanceof Integer) {
                statement.setInt(j, (Integer) parameter);
            } else if (parameter instanceof Double) {
                statement.setDouble(j, (Double) parameter);
            } else if (parameter instanceof Float) {
                statement.setFloat(j, (Float) parameter);
            } else if (parameter instanceof Boolean) {
                statement.setBoolean(j, (Boolean) parameter);
            } else {
                statement.setObject(j, parameter);
            }
        }

        return statement;
    }

    /**
     * Disconnect from database.
     */
    public void disconnect() {
        try {
            if (isConnected()) {
                connection.close();
            }
            hikariDataSource.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


}
