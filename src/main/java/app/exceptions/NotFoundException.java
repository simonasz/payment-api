package app.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class NotFoundException extends PaymentAPIException {
    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public int getStatus() {
        return HttpStatus.NOT_FOUND_404;
    }
}
