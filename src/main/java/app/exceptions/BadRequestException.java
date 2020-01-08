package app.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class BadRequestException extends PaymentAPIException {
    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public int getStatus() {
        return HttpStatus.BAD_REQUEST_400;
    }
}
