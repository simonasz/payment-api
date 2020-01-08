package test;

import app.constants.RouteConstants;
import app.exceptions.InvalidRequestData;
import app.models.Account;
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
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static app.constants.ErrorMessages.ERROR_COULD_NOT_FIND_TRANSACTION;
import static app.constants.ErrorMessages.ERROR_INSUFFICIENT_BALANCE;

public class TransactionFunctionalTest {
    private static String rootPathUrl;
    private TestData testData = new TestData();
    private Long nonExistingTransactionId = Long.valueOf(-123456);

    public TransactionFunctionalTest() throws SQLException {
    }

    @Before
    public void beforeEach() throws SQLException {
        rootPathUrl = "http://127.0.0.1:" + spark.Spark.port() + RouteConstants.Path.TRANSACTION;
        testData.deleteTestData();
    }

    @Test
    public void shouldTryToGetAllTransactions() throws IOException, SQLException {
        testData.createTestAccountTransactions();
        List<Transaction> existingTransactions = testData.getTestTransactions();

        Assert.assertNotEquals(0, existingTransactions.size());

        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl);

        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());
        List<Transaction> receivedTransactions =
                Arrays.asList(ObjectTransformer.getObject(apiClientResult.getResult(), Transaction[].class));

        Assert.assertEquals(existingTransactions.size(), receivedTransactions.size());
        existingTransactions.forEach(t -> {
            Optional<Transaction> rtOpt = receivedTransactions.stream().filter(rt -> t.equals(rt)).findFirst();
            Assert.assertNotNull(rtOpt.get());
            Validators.verifyTransactionEqualsFull(t, rtOpt.get());
        });
    }

    @Test
    public void shouldTryToGetTransactionById() throws IOException, SQLException {
        testData.createTestAccountTransactions();
        List<Transaction> existingTransactions = testData.getTestTransactions();

        Assert.assertNotEquals(0, existingTransactions.size());

        Transaction transactionToFind = existingTransactions.get(existingTransactions.size() - 1);

        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl + "/" + transactionToFind.getId());

        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());

        Transaction receivedTransaction = ObjectTransformer.getObject(apiClientResult.getResult(), Transaction.class);
        Assert.assertNotNull(receivedTransaction);
        Assert.assertEquals(transactionToFind, receivedTransaction);
        Validators.verifyTransactionEqualsFull(transactionToFind, receivedTransaction);
    }

    @Test
    public void shouldTryToGetNonExistingTransactionById() throws IOException, SQLException {
        testData.createTestAccountTransactions();
        List<Transaction> existingTransactions = testData.getTestTransactions();

        Assert.assertNotEquals(0, existingTransactions.size());

        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl + "/" + nonExistingTransactionId);

        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);

        Assert.assertEquals(HttpStatus.NOT_FOUND_404, apiClientResult.getStatus());
        Assert.assertEquals(ERROR_COULD_NOT_FIND_TRANSACTION, errorResponse.getError());
    }

    @Test
    public void shouldTryToCreateTransaction() throws IOException, InvalidRequestData, SQLException {
        testData.createTestAccountTransactions();
        List<Transaction> existingTransactions = testData.getTestTransactions();
        List<Account> existingAccounts = testData.getTestAccounts();

        Account senderAccount = existingAccounts.stream().findAny().get();
        Account receiverAccount = existingAccounts.stream().filter(a -> !a.equals(senderAccount)).findAny().get();

        //Check has money in account
        Assert.assertEquals(1, senderAccount.getBalance().compareTo(BigDecimal.ZERO));
        // Will transfer 1/3 of current balance
        BigDecimal amount = senderAccount.getBalance().divide(BigDecimal.valueOf(3), RoundingMode.HALF_DOWN);
        String title = "New transaction title";

        Transaction newTransactionReq = new Transaction();
        newTransactionReq.setTitle(title);
        newTransactionReq.setAmount(amount);
        newTransactionReq.setSenderAccountId(senderAccount.getId());
        newTransactionReq.setReceiverAccountId(receiverAccount.getId());

        ApiClient.ApiClientResult apiClientResult = ApiClient.post(rootPathUrl, newTransactionReq);

        //Verify new created transaction
        Assert.assertEquals(HttpStatus.CREATED_201, apiClientResult.getStatus());
        Transaction receivedTransaction = ObjectTransformer.getObject(apiClientResult.getResult(), Transaction.class);

        Assert.assertNotNull(receivedTransaction);
        Assert.assertEquals(title, receivedTransaction.getTitle());
        Assert.assertEquals(amount, receivedTransaction.getAmount());
        Assert.assertEquals(newTransactionReq.getSenderAccountId(), receivedTransaction.getSenderAccountId());
        Assert.assertEquals(newTransactionReq.getReceiverAccountId(), receivedTransaction.getReceiverAccountId());


        //Verify against database
        List<Transaction> existingTransactionsUpdated = testData.getTestTransactions();
        Assert.assertEquals(existingTransactions.size() + 1, existingTransactionsUpdated.size());
        Optional<Transaction> createdTransactionDBOpt =
                existingTransactionsUpdated.stream().filter(t -> t.equals(receivedTransaction)).findFirst();
        Assert.assertNotNull(createdTransactionDBOpt.get());
        Validators.verifyTransactionEqualsFull(createdTransactionDBOpt.get(), receivedTransaction);

        existingAccounts = testData.getTestAccounts();
        // Verify money was subtracted from account
        Account senderAccountUpd = existingAccounts.stream().filter(a -> a.equals(senderAccount)).findFirst().get();
        Assert.assertTrue(senderAccount.equals(senderAccountUpd));
        Assert.assertNotEquals(senderAccount.getBalance(), senderAccountUpd.getBalance());
        Assert.assertEquals(senderAccount.getBalance().subtract(amount), senderAccountUpd.getBalance());

        // Verify money was added to account
        Account receiverAccountUpd = existingAccounts.stream().filter(a -> a.equals(receiverAccount)).findAny().get();
        Assert.assertTrue(receiverAccount.equals(receiverAccountUpd));
        Assert.assertNotEquals(receiverAccount.getBalance(), receiverAccountUpd.getBalance());
        Assert.assertEquals(receiverAccount.getBalance().add(amount), receiverAccountUpd.getBalance());
    }

    @Ignore
    @Test
    public void shouldTryToCreateTransactionWithUnknownSender() {
        //TODO
    }

    @Ignore
    @Test
    public void shouldTryToCreateTransactionWithUnknownReceiver() {
        //TODO
    }

    @Test
    public void shouldTryToCreateTransactionWithInsufficientBalance() throws SQLException, IOException {
        testData.createTestAccountTransactions();
        List<Transaction> existingTransactions = testData.getTestTransactions();
        List<Account> existingAccounts = testData.getTestAccounts();

        Account senderAccount = existingAccounts.stream().findAny().get();
        Account receiverAccount = existingAccounts.stream().filter(a -> !a.equals(senderAccount)).findAny().get();

        //Check has money in account
        Assert.assertEquals(1, senderAccount.getBalance().compareTo(BigDecimal.ZERO));
        // Will transfer current amount + 1
        BigDecimal amount = senderAccount.getBalance().add(BigDecimal.ONE);
        String title = "New transaction title";

        Transaction newTransactionReq = new Transaction();
        newTransactionReq.setTitle(title);
        newTransactionReq.setAmount(amount);
        newTransactionReq.setSenderAccountId(senderAccount.getId());
        newTransactionReq.setReceiverAccountId(receiverAccount.getId());

        ApiClient.ApiClientResult apiClientResult = ApiClient.post(rootPathUrl, newTransactionReq);

        //Verify result
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, apiClientResult.getStatus());
        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);

        Assert.assertEquals(ERROR_INSUFFICIENT_BALANCE, errorResponse.getError());

        //Verify against database that balance did not change and no new transactions created
        List<Transaction> existingTransactionsUpdated = testData.getTestTransactions();
        Assert.assertEquals(existingTransactions.size(), existingTransactionsUpdated.size());

        existingAccounts = testData.getTestAccounts();
        // Verify sender account did not changed
        Account senderAccountUpd = existingAccounts.stream().filter(a -> a.equals(senderAccount)).findFirst().get();
        Validators.verifyAccountEqualsFull(senderAccount, senderAccountUpd);
        Assert.assertEquals(senderAccount.getBalance(), senderAccountUpd.getBalance());

        // Verify receiver account did not changed
        Account receiverAccountUpd = existingAccounts.stream().filter(a -> a.equals(receiverAccount)).findAny().get();
        Validators.verifyAccountEqualsFull(receiverAccount, receiverAccountUpd);
        Assert.assertEquals(receiverAccount.getBalance(), receiverAccountUpd.getBalance());
    }
}
