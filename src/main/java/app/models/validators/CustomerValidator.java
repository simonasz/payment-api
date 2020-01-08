package app.models.validators;

import app.exceptions.InvalidRequestData;
import app.models.Customer;

import static app.constants.ErrorMessages.ERROR_INVALID_PROPERTY_FIRSTNAME;
import static app.constants.ErrorMessages.ERROR_INVALID_PROPERTY_LASTNAME;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Manual validation class for validating Customer model
 */
public class CustomerValidator {
    /**
     * Manualy validate Customer model data
     *
     * @param customer
     * @throws InvalidRequestData
     */
    public static void validateCustomerData(Customer customer) throws InvalidRequestData {
        if (isNullOrEmpty(customer.getFirstName()))
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_FIRSTNAME);

        if (customer.getFirstName().length() > 255)
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_FIRSTNAME);

        if (isNullOrEmpty(customer.getLastName()))
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_LASTNAME);

        if (customer.getLastName().length() > 255)
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_LASTNAME);
    }
}
