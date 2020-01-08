package app.constants;

/**
 * Error message texts
 */
public class ErrorMessages {
    public static final String ERROR_COULD_NOT_FIND_ACCOUNT = "Could not find Account with provided id";
    public static final String ERROR_COULD_NOT_FIND_CUSTOMER = "Could not find Customer with provided id";
    public static final String ERROR_COULD_NOT_FIND_TRANSACTION = "Could not find Transaction with provided id";
    public static final String ERROR_INVALID_PROPERTY_TITLE = "Invalid value for property title. Value should be non " +
            "empty, between 1 and 255 characters";
    public static final String ERROR_INVALID_PROPERTY_FIRSTNAME = "Invalid value for property firstName. Value should" +
            " be non empty, between 1 and 255 characters";
    public static final String ERROR_INVALID_PROPERTY_LASTNAME = "Invalid value for property lastName. Value should " +
            "be non empty, between 1 and 255 characters";
    public static final String ERROR_INVALID_PROPERTY_CUSTOMER_ID = "Invalid value for customerId";
    public static final String ERROR_INVALID_PROPERTY_AMOUNT = "Property sender_account_id is required";
    public static final String ERROR_INVALID_PROPERTY_SENDER_ACCOUNT_ID = "Property sender_account_id is required";
    public static final String ERROR_INVALID_PROPERTY_RECEIVER_ACCOUNT_ID = "Property receiver_account_id is required";
    public static final String ERROR_SAME_ACCOUNT = "Sender can not be same as receiver";
    public static final String ERROR_UNKNOWN_SENDER = "Unknown sender account";
    public static final String ERROR_UNKNOWN_RECEIVER = "Unknown sender account";
    public static final String ERROR_INSUFFICIENT_BALANCE = "Unknown sender account";
    public static final String ERROR_NOT_IMPLEMENTED = "Not implemented";
    public static final String ERROR_NOT_FOUND = "Route not found";
    public static final String ERROR_INTERNAL_SERVER_ERROR = "Internal server error";
}
