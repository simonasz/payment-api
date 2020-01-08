package app.exceptions;

import org.eclipse.jetty.http.HttpStatus;

import static app.constants.ErrorMessages.ERROR_NOT_IMPLEMENTED;

/**
 * Not implemented exception
 */
public class NotImplementedException extends PaymentAPIException {
    public NotImplementedException() {
        super(ERROR_NOT_IMPLEMENTED);
    }

    @Override
    public int getStatus() {
        return HttpStatus.NOT_IMPLEMENTED_501;
    }
}
