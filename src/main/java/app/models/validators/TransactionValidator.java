package app.models.validators;

import app.exceptions.InvalidRequestData;
import app.models.Transaction;

import static app.constants.ErrorMessages.*;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Transaction object validator
 */
public class TransactionValidator {

    /**
     * Validate Transaction request data
     *
     * @param transaction
     * @throws InvalidRequestData
     */
    public static void validateTransactionData(Transaction transaction) throws InvalidRequestData {
        if (isNullOrEmpty(transaction.getTitle()))
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_TITLE);

        if (transaction.getTitle().length() > 255)
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_TITLE);

        if (transaction.getAmount() == null)
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_AMOUNT);

        if (transaction.getSenderAccountId() == null)
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_SENDER_ACCOUNT_ID);

        if (transaction.getReceiverAccountId() == null)
            throw new InvalidRequestData(ERROR_INVALID_PROPERTY_RECEIVER_ACCOUNT_ID);

        if (transaction.getSenderAccountId().equals(transaction.getReceiverAccountId()))
            throw new InvalidRequestData(ERROR_SAME_ACCOUNT);
    }
}
