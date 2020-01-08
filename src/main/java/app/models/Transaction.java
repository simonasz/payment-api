package app.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Transaction model
 * Represents money transfer between accounts
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    private Long id;
    private String title;
    private BigDecimal amount;
    private Long senderAccountId;
    private Long receiverAccountId;
    private Timestamp updated;
    private Timestamp created;

    public void setAmount(BigDecimal amount) {
        if (amount != null)
            this.amount = amount.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
