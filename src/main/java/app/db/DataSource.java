package app.db;

import app.exceptions.ApplicationException;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data source for H2 in memory database
 */
@Singleton
public class DataSource {
    private static Logger logger = LoggerFactory.getLogger(DataSource.class);
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    static {
        config.setJdbcUrl("jdbc:h2:~/payment");
        config.setUsername("");
        config.setPassword("");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    public DataSource() {
    }

    /**
     * Clean DB
     * Create schema
     *
     * @throws ApplicationException
     */
    public void cleanDB() {
        try (Connection con = ds.getConnection();
             Statement stm = con.createStatement()) {
            stm.execute("DROP ALL OBJECTS");
            stm.execute("" +
                    "CREATE TABLE IF NOT EXISTS customer (" +
                    "  id IDENTITY NOt NULL PRIMARY KEY," +
                    "  first_name VARCHAR(255) NOT NULL," +
                    "  last_name VARCHAR(255) NOT NULL," +
                    "  updated DATETIME NOT NULL," +
                    "  created DATETIME NOT NULL" +
                    ")");
            stm.execute("" +
                    "CREATE TABLE IF NOT EXISTS account (" +
                    "  id IDENTITY PRIMARY KEY," +
                    "  customer_id LONG NOT NULL," +
                    "  title VARCHAR(255) NOT NULL," +
                    "  balance DECIMAL NOT NULL," +
                    "  updated DATETIME NOT NULL," +
                    "  created DATETIME NOT NULL," +
                    "  foreign key ( customer_id ) references customer ( id )" +
                    ")");
            stm.execute("" +
                    "CREATE TABLE IF NOT EXISTS account_transaction (" +
                    "  id IDENTITY PRIMARY KEY," +
                    "  title VARCHAR(255) NOT NULL," +
                    "  amount DECIMAL NOT NULL," +
                    "  sender_account_id LONG NOT NULL," +
                    "  receiver_account_id LONG NOT NULL," +
                    "  updated DATETIME NOT NULL," +
                    "  created DATETIME NOT NULL," +
                    "  foreign key ( sender_account_id ) references account ( id )," +
                    "  foreign key ( receiver_account_id ) references account ( id )" +
                    ")");
        } catch (SQLException e) {
            logger.error("Error while preparing DB", e);
            throw new RuntimeException("Could not prepare Database");
        }
    }

    /**
     * Get connection instance
     *
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

}
