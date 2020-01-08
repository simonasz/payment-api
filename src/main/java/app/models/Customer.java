package app.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Customer model
 * Represents bank client. Can have multiple accounts
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {
    public Customer() {
    }

    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    private Long id;
    private String firstName;
    private String lastName;
    private Timestamp updated;
    private Timestamp created;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
