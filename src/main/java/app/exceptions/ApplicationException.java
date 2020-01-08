package app.exceptions;

import lombok.NoArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Generic application exception
 */
@NoArgsConstructor
public class ApplicationException extends PaymentAPIException {
    public ApplicationException(String message) {
        super(message);
    }

    @Override
    public int getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR_500;
    }
}