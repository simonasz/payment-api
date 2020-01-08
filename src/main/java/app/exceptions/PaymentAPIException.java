package app.exceptions;

import lombok.NoArgsConstructor;

/**
 * Base abstract class for Payment API exceptions
 */
@NoArgsConstructor
public abstract class PaymentAPIException extends Exception {
    private int status;

    public PaymentAPIException(String message) {
        super(message);
    }

    public abstract int getStatus();
}
