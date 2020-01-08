package app.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class DataConflictException extends PaymentAPIException {
    public DataConflictException(String message) {
        super(message);
    }

    @Override
    public int getStatus() {
        return HttpStatus.CONFLICT_409;
    }
}
