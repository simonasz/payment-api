package test;

import app.constants.ModelConstants;
import app.constants.RouteConstants;
import app.models.Account;
import app.models.Customer;
import app.models.Transaction;
import app.transformers.ObjectTransformer;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import utils.ApiClient;
import utils.TestData;
import utils.Validators;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static app.constants.ErrorMessages.ERROR_COULD_NOT_FIND_ACCOUNT;
import static app.constants.ErrorMessages.ERROR_COULD_NOT_FIND_CUSTOMER;

public class AccountFunctionalTest {
    private static String rootPathUrl;
    private TestData testData = new TestData();
    private Long nonExistingAccountId = Long.valueOf(-123456);

    public AccountFunctionalTest() throws SQLException {
    }

    @Before
    public void beforeEach() throws SQLException {
        rootPathUrl = "http://127.0.0.1:" + spark.Spark.port() + RouteConstants.Path.ACCOUNT;
        testData.deleteTestData();
    }

    @Test
    public void shouldTryToGetAllAccounts() throws IOException, SQLException {
        testData.createTestAccounts();
        List<Account> existingAccounts = testData.getTestAccounts();

        Assert.assertNotEquals(0, existingAccounts.size());

        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl);

        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());
        List<Account> receivedAccounts = Arrays.asList(ObjectTransformer.getObject(apiClientResult.getResult(),
                Account[].class));

        Assert.assertEquals(existingAccounts.size(), receivedAccounts.size());
        existingAccounts.forEach(a -> {
            Optional<Account> raOpt = receivedAccounts.stream().filter(ra -> a.equals(ra)).findFirst();
            Assert.assertNotNull(raOpt.get());
            Validators.verifyAccountEqualsFull(a, raOpt.get());
        });
    }

    @Test
    public void shouldTryToGetAccountById() throws IOException, SQLException {
        testData.createTestAccounts();
        List<Account> existingAccounts = testData.getTestAccounts();

        Assert.assertNotEquals(0, existingAccounts.size());

        Account accountToFind = existingAccounts.get(existingAccounts.size() - 1);

        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl + "/" + accountToFind.getId());

        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());

        Account receivedAccount = ObjectTransformer.getObject(apiClientResult.getResult(), Account.class);
        Assert.assertNotNull(receivedAccount);
        Assert.assertEquals(accountToFind, receivedAccount);
        Validators.verifyAccountEqualsFull(accountToFind, receivedAccount);
    }

    @Test
    public void shouldTryToGetNonExistingAccountById() throws IOException, SQLException {
        testData.createTestAccounts();
        List<Account> existingAccounts = testData.getTestAccounts();

        Assert.assertNotEquals(0, existingAccounts.size());

        Account accountToFind = existingAccounts.get(existingAccounts.size() - 1);

        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl + "/" + nonExistingAccountId);
        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);

        Assert.assertEquals(HttpStatus.NOT_FOUND_404, apiClientResult.getStatus());
        Assert.assertEquals(ERROR_COULD_NOT_FIND_ACCOUNT, errorResponse.getError());
    }

    @Test
    public void shouldTryToCreateAccount() throws IOException, SQLException {
        testData.createTestAccounts();
        List<Customer> existingCustomers = testData.getTestCustomers();
        Customer customer = existingCustomers.get(existingCustomers.size() - 1);

        List<Account> existingAccounts = testData.getTestAccounts();

        String newAccountTitle = String.format("New %s's account", customer.getFirstName());
        Account newAccountReq = new Account();
        newAccountReq.setTitle(newAccountTitle);
        //Add random value
        newAccountReq.setBalance(BigDecimal.valueOf(152448745));
        newAccountReq.setCustomerId(customer.getId());

        ApiClient.ApiClientResult apiClientResult = ApiClient.post(rootPathUrl, newAccountReq);

        //Verify new created account
        Assert.assertEquals(HttpStatus.CREATED_201, apiClientResult.getStatus());
        Account newAccount = ObjectTransformer.getObject(apiClientResult.getResult(), Account.class);

        Assert.assertEquals(newAccountTitle, newAccount.getTitle());
        Assert.assertEquals(customer.getId(), newAccount.getCustomerId());
        //verify default value was used
        Assert.assertEquals(ModelConstants.ACCOUNT_DEFAULT_BALANCE, newAccount.getBalance());

        //Verify against database
        List<Account> existingAccountsUpdated = testData.getTestAccounts();
        Assert.assertEquals(existingAccounts.size() + 1, existingAccountsUpdated.size());
        Optional<Account> createdAccountDBOpt =
                existingAccountsUpdated.stream().filter(a -> a.equals(newAccount)).findFirst();
        Assert.assertNotNull(createdAccountDBOpt.get());
        Validators.verifyAccountEqualsFull(createdAccountDBOpt.get(), newAccount);
    }

    @Ignore
    @Test
    public void shouldTryToCreateAccountWithInvalidNotProvidedCustomerId() {
        //TODO
    }

    @Test
    public void shouldTryToCreateAccountWithNonExistingUser() throws IOException, SQLException {
        testData.createTestAccounts();
        List<Customer> existingCustomers = testData.getTestCustomers();
        Assert.assertNotEquals(0, existingCustomers.size());

        List<Account> existingAccounts = testData.getTestAccounts();

        String newAccountTitle = String.format("New %s's account", "Tony");
        Account newAccountReq = new Account();
        newAccountReq.setTitle(newAccountTitle);
        //Add random value
        newAccountReq.setBalance(BigDecimal.valueOf(152448745));
        //Add non existing user id
        newAccountReq.setCustomerId(nonExistingAccountId);

        ApiClient.ApiClientResult apiClientResult = ApiClient.post(rootPathUrl, newAccountReq);
        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);

        //Verify error occured
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, apiClientResult.getStatus());
        Assert.assertEquals(ERROR_COULD_NOT_FIND_CUSTOMER, errorResponse.getError());

        //Verify against database
        List<Account> existingAccountsUpdated = testData.getTestAccounts();
        Assert.assertEquals(existingAccounts.size(), existingAccountsUpdated.size());
    }

    @Ignore
    @Test
    public void shouldTryToCreateAccountWithInvalidTitle() {
        //TODO
    }

    @Test
    public void shouldTryToUpdateAccount() throws IOException, SQLException {
        testData.createTestAccounts();

        List<Account> existingAccounts = testData.getTestAccounts();
        Account accountToUpdate = existingAccounts.get(existingAccounts.size() - 1);

        String newAccountTitle = String.format("%s updated", accountToUpdate.getTitle());
        Account updateAccountReq = new Account();
        updateAccountReq.setTitle(newAccountTitle);
        //Add random value Balance value
        updateAccountReq.setBalance(BigDecimal.valueOf(152448745));
        //Add random customer ID
        updateAccountReq.setCustomerId(nonExistingAccountId);

        ApiClient.ApiClientResult apiClientResult = ApiClient.patch(rootPathUrl + "/" + accountToUpdate.getId(),
                updateAccountReq);

        //Verify updated account
        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());
        Account updatedAccount = ObjectTransformer.getObject(apiClientResult.getResult(), Account.class);

        Assert.assertEquals(newAccountTitle, updatedAccount.getTitle());
        Assert.assertEquals(accountToUpdate.getCustomerId(), updatedAccount.getCustomerId());
        Assert.assertNotEquals(updateAccountReq.getCustomerId(), updatedAccount.getCustomerId());
        Assert.assertEquals(accountToUpdate.getBalance(), updatedAccount.getBalance());
        Assert.assertNotEquals(updateAccountReq.getBalance(), updatedAccount.getBalance());
        Assert.assertEquals(accountToUpdate.getCreated(), updatedAccount.getCreated());
        Assert.assertNotEquals(accountToUpdate.getUpdated(), updatedAccount.getUpdated());

        //Verify against database
        List<Account> existingAccountsUpdated = testData.getTestAccounts();
        Assert.assertEquals(existingAccounts.size(), existingAccountsUpdated.size());
        Optional<Account> updatedAccountDBOpt =
                existingAccountsUpdated.stream().filter(a -> a.equals(updatedAccount)).findFirst();
        Assert.assertNotNull(updatedAccountDBOpt.get());
        Validators.verifyAccountEqualsFull(updatedAccountDBOpt.get(), updatedAccount);
    }

    @Test
    public void shouldTryToUpdateNonExistingAccount() throws IOException, SQLException {
        testData.createTestAccounts();

        List<Account> existingAccounts = testData.getTestAccounts();
        Assert.assertNotEquals(0, existingAccounts.size());

        String newAccountTitle = String.format("%s updated", "Existing account");
        Account updateAccountReq = new Account();
        updateAccountReq.setTitle(newAccountTitle);

        ApiClient.ApiClientResult apiClientResult = ApiClient.patch(rootPathUrl + "/" + nonExistingAccountId,
                updateAccountReq);
        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);

        //Verify error occured
        Assert.assertEquals(HttpStatus.NOT_FOUND_404, apiClientResult.getStatus());
        Assert.assertEquals(ERROR_COULD_NOT_FIND_ACCOUNT, errorResponse.getError());

        //Verify against database
        List<Account> existingAccountsUpdated = testData.getTestAccounts();
        Assert.assertEquals(existingAccounts.size(), existingAccountsUpdated.size());
    }

    @Ignore
    @Test
    public void shouldTryToUpdateAccountWithInvalidTitle() {
        //TODO
    }

    @Ignore
    @Test
    public void shouldTryToDeleteAccountById() {
        //TODO
    }

    @Test
    public void shouldTryToGetAllAccountTransactions() throws IOException, SQLException {
        testData.createTestAccountTransactions();
        List<Account> existingAccounts = testData.getTestAccounts();
        Account account = existingAccounts.get(existingAccounts.size() - 1);
        List<Transaction> existingTransactions = testData.getTestTransactions();
        List<Transaction> accountTransactions =
                existingTransactions.stream().filter(t -> t.getSenderAccountId().equals(account.getId()) || t.getReceiverAccountId().equals(account.getId())).collect(Collectors.toList());

        Assert.assertNotEquals(0, accountTransactions.size());

        ApiClient.ApiClientResult apiClientResult =
                ApiClient.get(rootPathUrl + "/" + account.getId() + RouteConstants.Path.TRANSACTION);

        //Verify received transactions
        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());
        List<Transaction> receivedTransactions =
                Arrays.asList(ObjectTransformer.getObject(apiClientResult.getResult(), Transaction[].class));

        Assert.assertEquals(accountTransactions.size(), receivedTransactions.size());
        accountTransactions.forEach(t -> {
            Optional<Transaction> rtOpt = receivedTransactions.stream().filter(rt -> rt.equals(t)).findFirst();
            Assert.assertNotNull(rtOpt.get());
            Validators.verifyTransactionEqualsFull(t, rtOpt.get());
        });
    }

    @Test
    public void shouldTryToGetAllTransactionsOfNonExistingAccount() throws IOException, SQLException {
        testData.createTestAccounts();

        List<Account> existingAccounts = testData.getTestAccounts();
        Assert.assertNotEquals(0, existingAccounts.size());

        ApiClient.ApiClientResult apiClientResult =
                ApiClient.get(rootPathUrl + "/" + nonExistingAccountId + RouteConstants.Path.TRANSACTION);
        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);

        //Verify error occured
        Assert.assertEquals(HttpStatus.NOT_FOUND_404, apiClientResult.getStatus());
        Assert.assertEquals(ERROR_COULD_NOT_FIND_ACCOUNT, errorResponse.getError());
    }
}
