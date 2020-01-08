package app.services;

import app.db.DataSource;
import app.exceptions.DataBaseException;
import app.models.Customer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class CustomerService {
    @Inject
    private DataSource dataSource;

    /**
     * Get all customers
     *
     * @return
     * @throws DataBaseException
     */
    public List<Customer> getCustomers() throws DataBaseException {
        String sql = "SELECT * FROM customer";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            ResultSet rs = pst.executeQuery();

            List<Customer> result = new ArrayList<>();
            while (rs.next()) {
                result.add(createCustomerFromResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Get customer by id
     *
     * @param id
     * @return
     * @throws DataBaseException
     */
    public Customer getCustomer(Long id) throws DataBaseException {
        String sql = "SELECT * FROM customer WHERE id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setLong(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next())
                return createCustomerFromResultSet(rs);
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
        return null;
    }

    /**
     * Create a new customer
     *
     * @param customer
     * @return
     * @throws DataBaseException
     */
    public Customer createCustomer(Customer customer) throws DataBaseException {
        String sql = "INSERT INTO customer (first_name, last_name, updated, created) VALUES (?, ?, NOW(), NOW())";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, customer.getFirstName());
            pst.setString(2, customer.getLastName());
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new DataBaseException("Could not create user. No rows affected");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next())
                    return this.getCustomer(generatedKeys.getLong(1));
                else
                    throw new DataBaseException("Could not obtain newly created user ID");
            }
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Update customer
     *
     * @param customer
     * @return
     * @throws DataBaseException
     */
    public Customer updateCustomer(Customer customer) throws DataBaseException {
        String sql = "UPDATE customer SET first_name = ?, last_name = ?, updated = NOW() WHERE id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, customer.getFirstName());
            pst.setString(2, customer.getLastName());
            pst.setLong(3, customer.getId());
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new DataBaseException("Could not update user. No rows affected");
            }
            return this.getCustomer(customer.getId());
        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    /**
     * Create Customer object from ResultSet
     *
     * @param res
     * @return
     * @throws SQLException
     */
    private Customer createCustomerFromResultSet(ResultSet res) throws SQLException {
        Customer customer = new Customer();
        customer.setId(res.getLong("id"));
        customer.setFirstName(res.getString("first_name"));
        customer.setLastName(res.getString("last_name"));
        customer.setUpdated(res.getTimestamp("updated"));
        customer.setCreated(res.getTimestamp("created"));
        return customer;
    }
}
