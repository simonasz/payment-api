package app.services;

import app.db.DataSource;
import app.exceptions.DataBaseException;
import app.models.Account;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Account model service
 * Used for integration with DB
 */
@Singleton
public class AccountService {
    @Inject
    private DataSource dataSource;

    /**
     * Get all accounts from DB
     *
     * @return
     * @throws DataBaseException
     */
    public List<Account> getAccounts() throws DataBaseException {
        String sql = "SELECT * FROM account";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            ResultSet rs = pst.executeQuery();

            List<Account> result = new ArrayList<>();
            while (rs.next()) {
                result.add(createAccountFromResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Get Acount by customer id
     *
     * @param customerId
     * @return
     * @throws DataBaseException
     */
    public List<Account> getAccountsByCustomerId(Long customerId) throws DataBaseException {
        String sql = "SELECT * FROM account WHERE customer_id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setLong(1, customerId);
            ResultSet rs = pst.executeQuery();

            List<Account> result = new ArrayList<>();
            while (rs.next()) {
                result.add(createAccountFromResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Get Account by id
     *
     * @param id
     * @return
     * @throws DataBaseException
     */
    public Account getAccount(Long id) throws DataBaseException {
        String sql = "SELECT * FROM account WHERE id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next())
                return createAccountFromResultSet(rs);
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
        return null;
    }

    /**
     * Create new Account
     *
     * @param account
     * @return
     * @throws DataBaseException
     */
    public Account createAccount(Account account) throws DataBaseException {
        String sql = "INSERT INTO account (customer_id, title, balance, updated, created) VALUES (?, ?, ?, NOW(), NOW" +
                "())";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pst.setLong(1, account.getCustomerId());
            pst.setString(2, account.getTitle());
            pst.setBigDecimal(3, account.getBalance());
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0)
                throw new DataBaseException("Could not create account. No rows affected");

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next())
                    return this.getAccount(generatedKeys.getLong(1));
                else
                    throw new DataBaseException("Could not obtain newly created account ID");
            }
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Update existing account
     *
     * @param account
     * @return
     * @throws DataBaseException
     */
    public Account updateAccount(Account account) throws DataBaseException {
        String sql = "UPDATE account SET title = ?, updated = NOW() WHERE id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, account.getTitle());
            pst.setLong(2, account.getId());
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0)
                throw new DataBaseException("Could not update account. No rows affected");
            return this.getAccount(account.getId());
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Create Account from ResultSet object
     *
     * @param res
     * @return
     * @throws SQLException
     */
    private static Account createAccountFromResultSet(ResultSet res) throws SQLException {
        Account account = new Account();
        account.setId(res.getLong("id"));
        account.setCustomerId(res.getLong("customer_id"));
        account.setTitle(res.getString("title"));
        account.setBalance(res.getBigDecimal("balance"));
        account.setUpdated(res.getTimestamp("updated"));
        account.setCreated(res.getTimestamp("created"));
        return account;
    }
}
