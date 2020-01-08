package app.services;

import app.db.DataSource;
import app.exceptions.DataBaseException;
import app.exceptions.DataConflictException;
import app.models.Account;
import app.models.Transaction;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction service for accessing and modifications in DB for @Transaction entity
 */
@Singleton
public class TransactionService {
    @Inject
    private DataSource dataSource;

    /**
     * Get all account transactions
     *
     * @return
     * @throws DataBaseException
     */
    public List<Transaction> getTransactions() throws DataBaseException {
        String sql = "SELECT * FROM account_transaction";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            ResultSet rs = pst.executeQuery();

            List<Transaction> result = new ArrayList<>();
            while (rs.next()) {
                result.add(createTransactionFromResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Get transactions by sender or receiver account
     *
     * @param accountId
     * @return
     * @throws DataBaseException
     */
    public List<Transaction> getTransactionsByAccountId(Long accountId) throws DataBaseException {
        String sql = "SELECT * FROM account_transaction WHERE sender_account_id = ? OR receiver_account_id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setLong(1, accountId);
            pst.setLong(2, accountId);
            ResultSet rs = pst.executeQuery();

            List<Transaction> result = new ArrayList<>();
            while (rs.next()) {
                result.add(createTransactionFromResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Create new Transaction
     * Creates 1 Transaction, updates 2 Account's in single DB transaction
     * To ensure account data integrity (not outdated), uses 'created' attribute
     *
     * @param sender
     * @param receiver
     * @param transaction
     * @return
     * @throws DataBaseException
     */
    public Transaction createTransaction(Account sender, Account receiver, Transaction transaction) throws DataBaseException {
        Transaction result;
        String sqlTransaction = "INSERT INTO account_transaction (title, amount, sender_account_id, " +
                "receiver_account_id, updated, created) VALUES (?, ?, ?, ?, NOW(), NOW())";
        String sqlAccount = "UPDATE account SET balance = ?, updated = NOW() WHERE id = ? AND updated = ?";
        try (
                Connection con = dataSource.getConnection();
                PreparedStatement pstTransaction = con.prepareStatement(sqlTransaction,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement pstSender = con.prepareStatement(sqlAccount);
                PreparedStatement pstReceiver = con.prepareStatement(sqlAccount)) {

            //Turn off auto commit. Need all 3 statements to be executed
            con.setAutoCommit(false);

            // Set transaction data
            pstTransaction.setString(1, transaction.getTitle());
            pstTransaction.setBigDecimal(2, transaction.getAmount());
            pstTransaction.setLong(3, transaction.getSenderAccountId());
            pstTransaction.setLong(4, transaction.getReceiverAccountId());
            int affectedRowsTransaction = pstTransaction.executeUpdate();
            if (affectedRowsTransaction == 0)
                throw new DataBaseException("Could not create transaction.");

            //Sender account data
            pstSender.setBigDecimal(1, sender.getBalance());
            pstSender.setLong(2, sender.getId());
            pstSender.setTimestamp(3, sender.getUpdated());
            int affectedRowsSender = pstSender.executeUpdate();
            if (affectedRowsSender == 0)
                throw new DataConflictException("Could not update sender account.");

            //Receiver account data
            pstReceiver.setBigDecimal(1, receiver.getBalance());
            pstReceiver.setLong(2, receiver.getId());
            pstReceiver.setTimestamp(3, receiver.getUpdated());
            int affectedRowsReceiver = pstReceiver.executeUpdate();
            if (affectedRowsReceiver == 0)
                throw new DataConflictException("Could not update receiver account.");

            con.commit();

            try (ResultSet generatedKeys = pstTransaction.getGeneratedKeys()) {
                if (generatedKeys.next())
                    result = this.getTransaction(generatedKeys.getLong(1));
                else
                    throw new DataBaseException("Could not obtain newly created transaction ID");
            }


            return result;
        } catch (SQLException | DataConflictException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Get transaction by id
     *
     * @param id
     * @return
     * @throws DataBaseException
     */
    public Transaction getTransaction(Long id) throws DataBaseException {
        String sql = "SELECT * FROM account_transaction WHERE id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next())
                return createTransactionFromResultSet(rs);
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
        return null;
    }

    /**
     * Create Transaction object from ResultSet
     *
     * @param res
     * @return
     * @throws SQLException
     */
    private Transaction createTransactionFromResultSet(ResultSet res) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(res.getLong("id"));
        transaction.setTitle(res.getString("title"));
        transaction.setAmount(res.getBigDecimal("amount"));
        transaction.setSenderAccountId(res.getLong("sender_account_id"));
        transaction.setReceiverAccountId(res.getLong("receiver_account_id"));
        transaction.setUpdated(res.getTimestamp("updated"));
        transaction.setCreated(res.getTimestamp("created"));
        return transaction;
    }
}
