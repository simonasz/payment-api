package utils;

import app.models.Account;
import app.models.Customer;
import app.models.Transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Validators for functional/unit tests
 */
public class Validators {
    /**
     * Verify Customer equals. Check only first and last names
     *
     * @param expectedCustomer
     * @param receivedCustomer
     */
    public static void verifyCustomerEquals(Customer expectedCustomer, Customer receivedCustomer) {
        assertNotNull(receivedCustomer);
        assertEquals(expectedCustomer.getFirstName(), receivedCustomer.getFirstName());
        assertEquals(expectedCustomer.getLastName(), receivedCustomer.getLastName());
    }

    /**
     * Verify Customer objects equals
     * Checks all attributes
     *
     * @param expectedCustomer
     * @param receivedCustomer
     */
    public static void verifyCustomerEqualsFull(Customer expectedCustomer, Customer receivedCustomer) {
        assertNotNull(receivedCustomer);
        assertEquals(expectedCustomer.getId(), receivedCustomer.getId());
        assertEquals(expectedCustomer.getFirstName(), receivedCustomer.getFirstName());
        assertEquals(expectedCustomer.getLastName(), receivedCustomer.getLastName());
        assertEquals(expectedCustomer.getCreated(), receivedCustomer.getCreated());
        assertEquals(expectedCustomer.getUpdated(), receivedCustomer.getUpdated());
    }

    /**
     * Verify Account objects equals
     * Check all attributes
     *
     * @param expectedAccount
     * @param receivedAccount
     */
    public static void verifyAccountEqualsFull(Account expectedAccount, Account receivedAccount) {
        assertNotNull(receivedAccount);
        assertEquals(expectedAccount.getId(), receivedAccount.getId());
        assertEquals(expectedAccount.getCustomerId(), receivedAccount.getCustomerId());
        assertEquals(expectedAccount.getTitle(), receivedAccount.getTitle());
        assertEquals(expectedAccount.getBalance(), receivedAccount.getBalance());
        assertEquals(expectedAccount.getUpdated(), receivedAccount.getUpdated());
        assertEquals(expectedAccount.getCreated(), receivedAccount.getCreated());
    }

    /**
     * Verify Transaction objects equal
     *
     * @param expectedTransaction
     * @param receivedTransaction
     */
    public static void verifyTransactionEqualsFull(Transaction expectedTransaction, Transaction receivedTransaction) {
        assertNotNull(receivedTransaction);
        assertEquals(expectedTransaction.getId(), receivedTransaction.getId());
        assertEquals(expectedTransaction.getTitle(), receivedTransaction.getTitle());
        assertEquals(expectedTransaction.getAmount(), receivedTransaction.getAmount());
        assertEquals(expectedTransaction.getSenderAccountId(), receivedTransaction.getSenderAccountId());
        assertEquals(expectedTransaction.getReceiverAccountId(), receivedTransaction.getReceiverAccountId());
        assertEquals(expectedTransaction.getUpdated(), receivedTransaction.getUpdated());
        assertEquals(expectedTransaction.getCreated(), receivedTransaction.getCreated());
    }
}
