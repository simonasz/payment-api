package app.models.validators;

import app.exceptions.InvalidRequestData;
import app.models.Account;

import static app.constants.ErrorMessages.ERROR_INVALID_PROPERTY_CUSTOMER_ID;
import static app.constants.ErrorMessages.ERROR_INVALID_PROPERTY_TITLE;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Manual validator class for Account model
 */
public class AccountValidator {
    /**
     * Manually validate Account model data
     *
     * @param account
     * @throws InvalidRequestData
     */
    public static void validateAccountData(Account account) throws InvalidRequestData {
        if (isNullOrEmpty(account.getTitle()))
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_TITLE);

        if (account.getTitle().length() > 255)
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_TITLE);

        if (account.getCustomerId() == null)
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_CUSTOMER_ID);
    }
}
