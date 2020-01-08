package app.controllers;

import app.constants.RouteConstants;
import app.exceptions.InvalidRequestData;
import app.exceptions.NotFoundException;
import app.models.Account;
import app.models.Transaction;
import app.models.validators.TransactionValidator;
import app.services.AccountService;
import app.services.TransactionService;
import app.transformers.ObjectTransformer;
import app.utils.RequestUtils;
import com.google.inject.Inject;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.RouteGroup;

import java.util.List;
import java.util.Optional;

import static app.constants.ErrorMessages.*;
import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Transaction controller
 */
public class TransactionController implements RouteGroup {
    @Inject
    private AccountService accountService;
    @Inject
    private TransactionService transactionService;

    @Override
    public void addRoutes() {
        get(RouteConstants.Path.EMPTY, this.getAllTransactions);
        post(RouteConstants.Path.EMPTY, this.createTransaction);
        get(RouteConstants.Path.SEPARATOR + RouteConstants.Params.TRANSACTION_ID, this.getTransaction);
    }

    /**
     * Get all transactions
     */
    private Route getAllTransactions = (Request req, Response res) -> {
        List<Transaction> transactions = this.transactionService.getTransactions();
        res.status(HttpStatus.OK_200);
        return ObjectTransformer.objectToString(transactions);
    };

    /**
     * Get transaction by id
     */
    private Route getTransaction = (Request req, Response res) -> {
        Long transactionId = RequestUtils.getIdFromRequest(req, RouteConstants.Params.TRANSACTION_ID);

        Optional<Transaction> result = Optional.ofNullable(transactionService.getTransaction(transactionId));
        if (!result.isPresent())
            throw new NotFoundException(ERROR_COULD_NOT_FIND_TRANSACTION);
        res.status(HttpStatus.OK_200);
        return ObjectTransformer.objectToString(result.get());
    };

    /**
     * Create transaction
     */
    private Route createTransaction = (Request req, Response res) -> {
        Transaction createTransaction = ObjectTransformer.getRequestObject(req.body(), Transaction.class);
        TransactionValidator.validateTransactionData(createTransaction);

        Optional<Account> receiverAccountOpt =
                Optional.ofNullable(this.accountService.getAccount(createTransaction.getReceiverAccountId()));
        if (!receiverAccountOpt.isPresent())
            throw new InvalidRequestData(ERROR_UNKNOWN_RECEIVER);
        Account receiverAccount = receiverAccountOpt.get();

        Optional<Account> senderAccountOpt =
                Optional.ofNullable(this.accountService.getAccount(createTransaction.getSenderAccountId()));
        if (!senderAccountOpt.isPresent())
            throw new InvalidRequestData(ERROR_UNKNOWN_SENDER);
        Account senderAccount = senderAccountOpt.get();

        if (createTransaction.getAmount().compareTo(senderAccount.getBalance()) > 0)
            throw new InvalidRequestData(ERROR_INSUFFICIENT_BALANCE);

        //Increase receiver balance
        receiverAccount.setBalance(receiverAccount.getBalance().add(createTransaction.getAmount()));
        //Decrease sender balance
        senderAccount.setBalance(senderAccount.getBalance().subtract(createTransaction.getAmount()));

        //Try to save
        createTransaction = transactionService.createTransaction(senderAccount, receiverAccount, createTransaction);

        res.status(HttpStatus.CREATED_201);
        return ObjectTransformer.objectToString(createTransaction);
    };
}
