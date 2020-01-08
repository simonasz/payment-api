package app.exceptions;

import lombok.NoArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;

@NoArgsConstructor
public class InvalidRequestData extends PaymentAPIException {
    public InvalidRequestData(String message) {
        super(message);
    }

    @Override
    public int getStatus() {
        return HttpStatus.BAD_REQUEST_400;
    }
}
