package utils;

import app.constants.ModelConstants;
import app.db.DataSource;
import app.models.Account;
import app.models.Customer;
import app.models.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestData {
    private DataSource dataSource;

    public TestData() throws SQLException {
        this.dataSource = new DataSource();
    }

    public void deleteTestData() throws SQLException {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute("DELETE FROM account_transaction");
            st.execute("DELETE FROM account");
            st.execute("DELETE FROM customer");
        }
    }

    public void createTestCustomers() throws SQLException {
        String sql = "INSERT INTO customer (first_name, last_name, updated, created) VALUES (?, ?, NOW(), NOW())";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, "John");
            pst.setString(2, "Doe");
            pst.execute();
            pst.setString(1, "Jane");
            pst.setString(2, "Doe");
            pst.execute();
            pst.setString(1, "Mike");
            pst.setString(2, "Doe");
            pst.execute();
        }
    }

    public List<Customer> getTestCustomers() throws SQLException {
        List<Customer> result = new ArrayList<Customer>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT * FROM customer")) {
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                result.add(createCustomerFromResultSet(rs));
            }
        }
        return result;
    }

    public void createTestAccounts() throws SQLException {
        this.createTestCustomers();
        List<Customer> customers = this.getTestCustomers();
        String sql = "INSERT INTO account (customer_id, title, balance, updated, created) VALUES (?, ?, ?, NOW(), NOW" +
                "())";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            for (Customer c : customers) {
                pst.setLong(1, c.getId());
                pst.setString(2, String.format("%s's savings account", c.getFirstName()));
                pst.setBigDecimal(3, ModelConstants.ACCOUNT_DEFAULT_BALANCE);
                pst.execute();
                pst.setLong(1, c.getId());
                pst.setString(2, String.format("%s's family account", c.getFirstName()));
                pst.setBigDecimal(3, ModelConstants.ACCOUNT_DEFAULT_BALANCE);
                pst.execute();
                pst.setLong(1, c.getId());
                pst.setString(2, String.format("%s's emergency account", c.getFirstName()));
                pst.setBigDecimal(3, ModelConstants.ACCOUNT_DEFAULT_BALANCE);
                pst.execute();
            }
        }
    }

    public List<Account> getTestAccounts() throws SQLException {
        List<Account> result = new ArrayList<Account>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT * FROM account")) {
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                result.add(createAccountFromResultSet(rs));
            }
        }
        return result;
    }

    public void createTestAccountTransactions() throws SQLException {
        this.createTestAccounts();
        List<Account> accounts = this.getTestAccounts();
        String sql = "INSERT INTO account_transaction (title, amount, sender_account_id, receiver_account_id, " +
                "updated, created) VALUES (?, ?, ?, ?, NOW(), NOW())";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            for (Account a : accounts) {
                pst.setString(1, String.format("Rent for last month"));
                pst.setBigDecimal(2, BigDecimal.valueOf(124.36));
                pst.setLong(3, a.getId());
                pst.setLong(4, accounts.stream().filter(aa -> !a.equals(aa)).findAny().get().getId());
                pst.execute();
                pst.setString(1, String.format("Payment for TV services"));
                pst.setBigDecimal(2, BigDecimal.valueOf(12.48));
                pst.setLong(3, a.getId());
                pst.setLong(4, accounts.stream().filter(aa -> !a.equals(aa)).findAny().get().getId());
                pst.execute();
                pst.setString(1, String.format("Debt return"));
                pst.setBigDecimal(2, BigDecimal.valueOf(230));
                pst.setLong(3, accounts.stream().filter(aa -> !a.equals(aa)).findAny().get().getId());
                pst.setLong(4, a.getId());
                pst.execute();
            }
        }
    }

    public List<Transaction> getTestTransactions() throws SQLException {
        List<Transaction> result = new ArrayList<Transaction>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT * FROM account_transaction")) {
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                result.add(createTransactionFromResultSet(rs));
            }
        }
        return result;
    }

    private Customer createCustomerFromResultSet(ResultSet res) throws SQLException {
        Customer customer = new Customer();
        customer.setId(res.getLong("id"));
        customer.setFirstName(res.getString("first_name"));
        customer.setLastName(res.getString("last_name"));
        customer.setUpdated(res.getTimestamp("updated"));
        customer.setCreated(res.getTimestamp("created"));
        return customer;
    }

    private Account createAccountFromResultSet(ResultSet res) throws SQLException {
        Account account = new Account();
        account.setId(res.getLong("id"));
        account.setCustomerId(res.getLong("customer_id"));
        account.setTitle(res.getString("title"));
        account.setBalance(res.getBigDecimal("balance"));
        account.setUpdated(res.getTimestamp("updated"));
        account.setCreated(res.getTimestamp("created"));
        return account;
    }

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
