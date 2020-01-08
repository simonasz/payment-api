package app.exceptions;

import lombok.NoArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;

@NoArgsConstructor
public class DataBaseException extends PaymentAPIException {
    public DataBaseException(String message) {
        super(message);
    }

    @Override
    public int getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR_500;
    }
}
