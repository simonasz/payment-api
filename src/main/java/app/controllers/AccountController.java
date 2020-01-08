package app.controllers;

import app.constants.ModelConstants;
import app.constants.RouteConstants;
import app.exceptions.InvalidRequestData;
import app.exceptions.NotFoundException;
import app.exceptions.NotImplementedException;
import app.models.Account;
import app.models.Customer;
import app.models.Transaction;
import app.models.validators.AccountValidator;
import app.services.AccountService;
import app.services.CustomerService;
import app.services.TransactionService;
import app.transformers.ObjectTransformer;
import app.utils.RequestUtils;
import com.google.inject.Inject;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.RouteGroup;

import java.util.List;
import java.util.Optional;

import static app.constants.ErrorMessages.ERROR_COULD_NOT_FIND_ACCOUNT;
import static app.constants.ErrorMessages.ERROR_COULD_NOT_FIND_CUSTOMER;
import static spark.Spark.*;

/**
 * Account controller for managing Acounts
 */
public class AccountController implements RouteGroup {
    @Inject
    private AccountService accountService;
    @Inject
    private CustomerService customerService;
    @Inject
    private TransactionService transactionService;

    /**
     * Create AccountController routes
     */
    @Override
    public void addRoutes() {
        get(RouteConstants.Path.EMPTY, MimeTypes.Type.APPLICATION_JSON.asString(), this.getAllAccounts);
        post(RouteConstants.Path.EMPTY, MimeTypes.Type.APPLICATION_JSON.asString(), this.createAccount);
        get(RouteConstants.Path.SEPARATOR + RouteConstants.Params.ACCOUNT_ID,
                MimeTypes.Type.APPLICATION_JSON.asString(), this.getAccount);
        patch(RouteConstants.Path.SEPARATOR + RouteConstants.Params.ACCOUNT_ID,
                MimeTypes.Type.APPLICATION_JSON.asString(), this.updateAccount);
        delete(RouteConstants.Path.SEPARATOR + RouteConstants.Params.ACCOUNT_ID,
                MimeTypes.Type.APPLICATION_JSON.asString(), this.notImplemented);
        get(RouteConstants.Path.SEPARATOR + RouteConstants.Params.ACCOUNT_ID + RouteConstants.Path.TRANSACTION,
                MimeTypes.Type.APPLICATION_JSON.asString(), this.getAccountTransactions);
    }

    /**
     * Get all account despite of customer
     */
    private Route getAllAccounts = (Request req, Response res) -> {
        List<Account> result = this.accountService.getAccounts();
        return ObjectTransformer.objectToString(result);
    };

    /**
     * Get specific account by id
     */
    private Route getAccount = (Request req, Response res) -> {
        Long accountId = RequestUtils.getIdFromRequest(req, RouteConstants.Params.ACCOUNT_ID);

        Optional<Account> result = Optional.ofNullable(accountService.getAccount(accountId));
        if (!result.isPresent())
            throw new NotFoundException(ERROR_COULD_NOT_FIND_ACCOUNT);
        res.status(HttpStatus.OK_200);
        return ObjectTransformer.objectToString(result.get());
    };

    /**
     * Create new account
     */
    private Route createAccount = (Request req, Response res) -> {
        Account account = ObjectTransformer.getRequestObject(req.body(), Account.class);

        AccountValidator.validateAccountData(account);

        Optional<Customer> customer = Optional.ofNullable(customerService.getCustomer(account.getCustomerId()));
        if (!customer.isPresent())
            throw new InvalidRequestData(ERROR_COULD_NOT_FIND_CUSTOMER);

        account.setBalance(ModelConstants.ACCOUNT_DEFAULT_BALANCE);

        account = accountService.createAccount(account);

        res.status(HttpStatus.CREATED_201);
        return ObjectTransformer.objectToString(account);
    };

    /**
     * Update existing account
     */
    private Route updateAccount = (Request req, Response res) -> {
        Long accountId = RequestUtils.getIdFromRequest(req, RouteConstants.Params.ACCOUNT_ID);

        Optional<Account> existingAccount = Optional.ofNullable(accountService.getAccount(accountId));
        if (!existingAccount.isPresent())
            throw new NotFoundException(ERROR_COULD_NOT_FIND_ACCOUNT);

        Account updateAccount = ObjectTransformer.getRequestObject(req.body(), Account.class);
        updateAccount.setId(existingAccount.get().getId());
        updateAccount.setCustomerId(existingAccount.get().getCustomerId());
        if (updateAccount.getTitle() == null)
            updateAccount.setTitle(existingAccount.get().getTitle());

        AccountValidator.validateAccountData(updateAccount);

        updateAccount = accountService.updateAccount(updateAccount);
        res.status(HttpStatus.OK_200);
        return ObjectTransformer.objectToString(updateAccount);
    };

    /**
     * Not implemented method handler
     */
    private Route notImplemented = (Request req, Response res) -> {
        throw new NotImplementedException();
    };

    /**
     * Get account transactions
     */
    private Route getAccountTransactions = (Request req, Response res) -> {
        Long accountId = RequestUtils.getIdFromRequest(req, RouteConstants.Params.ACCOUNT_ID);

        Optional<Account> existingAccount = Optional.ofNullable(accountService.getAccount(accountId));
        if (!existingAccount.isPresent())
            throw new NotFoundException(ERROR_COULD_NOT_FIND_ACCOUNT);

        List<Transaction> transactions =
                this.transactionService.getTransactionsByAccountId(existingAccount.get().getId());

        res.status(HttpStatus.OK_200);
        return ObjectTransformer.objectToString(transactions);
    };
}
