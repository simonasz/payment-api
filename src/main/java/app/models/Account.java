package app.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Account model
 * Represents bank account. Belongs to customer
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
    private Long id;
    private Long customerId;
    private String title;
    private BigDecimal balance;
    private Timestamp updated;
    private Timestamp created;

    public void setBalance(BigDecimal balance) {
        if (balance != null)
            this.balance = balance.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        this.balance = balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
