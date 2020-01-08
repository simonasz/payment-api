package app.controllers;

import app.TestException;
import app.constants.RouteConstants;
import app.exceptions.ApplicationException;
import app.exceptions.DataBaseException;
import app.exceptions.NotFoundException;
import app.models.Account;
import app.services.AccountService;
import app.services.CustomerService;
import app.services.TransactionService;
import app.transformers.ObjectTransformer;
import app.utils.RequestUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.List;

import static app.constants.ErrorMessages.ERROR_COULD_NOT_FIND_ACCOUNT;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({spark.Spark.class, ObjectTransformer.class, RequestUtils.class})
public class AccountControllerTest {
    @Mock
    private AccountService accountServiceMock;
    @Mock
    private CustomerService customerServiceMock;
    @Mock
    private TransactionService transactionServiceMock;

    private AccountController accountController;


    @Mock
    private Request req;
    @Mock
    private Response res;

    private String expectedResult = "custom expected result";
    private Long accountId = Long.valueOf(1458745);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(spark.Spark.class, ObjectTransformer.class, RequestUtils.class);

        // Custom Guice injector with mocked values
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                this.bind(AccountService.class).toInstance(accountServiceMock);
                this.bind(CustomerService.class).toInstance(customerServiceMock);
                this.bind(TransactionService.class).toInstance(transactionServiceMock);
            }
        });
        accountController = injector.getInstance(AccountController.class);
    }

    @Test
    public void testServicesInjected() {
        AccountService accountService = Whitebox.getInternalState(accountController, "accountService");
        Assert.assertEquals(accountServiceMock, accountService);

        CustomerService customerService = Whitebox.getInternalState(accountController, "customerService");
        Assert.assertEquals(customerServiceMock, customerService);

        TransactionService transactionService = Whitebox.getInternalState(accountController, "transactionService");
        Assert.assertEquals(transactionServiceMock, transactionService);
    }

    @Test(expected = TestException.class)
    public void testAllAccountRouteAdded() throws Exception {
        TestException testException = new TestException();
        Route getAllAccountsImpl = Whitebox.getInternalState(accountController, "getAllAccounts");
        PowerMockito.doThrow(testException).when(spark.Spark.class, "get", RouteConstants.Path.EMPTY,
                MimeTypes.Type.APPLICATION_JSON.asString(), getAllAccountsImpl);
        try {
            accountController.addRoutes();
        } catch (Exception e) {
            Assert.assertEquals(testException, e);
            throw e;
        }
    }

    @Test(expected = TestException.class)
    public void testCreateAccountRouteAdded() throws Exception {
        TestException testException = new TestException();
        Route createAccountImpl = Whitebox.getInternalState(accountController, "createAccount");
        PowerMockito.doThrow(testException).when(spark.Spark.class, "post", RouteConstants.Path.EMPTY,
                MimeTypes.Type.APPLICATION_JSON.asString(), createAccountImpl);
        try {
            accountController.addRoutes();
        } catch (Exception e) {
            Assert.assertEquals(testException, e);
            throw e;
        }
    }

    @Test
    public void testGetAllAccounts() throws Exception {
        List<Account> accountsMock = new ArrayList<>();
        when(accountServiceMock.getAccounts()).thenReturn(accountsMock);
        when(ObjectTransformer.objectToString(accountsMock)).thenReturn(expectedResult);

        Route getAllAccountsImpl = Whitebox.getInternalState(accountController, "getAllAccounts");
        Object result = getAllAccountsImpl.handle(req, res);

        Assert.assertEquals(expectedResult, result);
    }

    @Test(expected = DataBaseException.class)
    public void testGetAllAccountsWithDbException() throws Exception {
        DataBaseException ex = new DataBaseException();
        when(accountServiceMock.getAccounts()).thenThrow(ex);

        Route getAllAccountsImpl = Whitebox.getInternalState(accountController, "getAllAccounts");

        try {
            getAllAccountsImpl.handle(req, res);
        } catch (DataBaseException e) {
            Assert.assertEquals(ex, e);
            throw e;
        }
    }

    @Test
    public void testGetAccountById() throws Exception {
        Account accountMock = Mockito.mock(Account.class);
        when(RequestUtils.getIdFromRequest(req, RouteConstants.Params.ACCOUNT_ID)).thenReturn(accountId);
        when(accountServiceMock.getAccount(accountId)).thenReturn(accountMock);
        when(ObjectTransformer.objectToString(accountMock)).thenReturn(expectedResult);

        Route getAccountImpl = Whitebox.getInternalState(accountController, "getAccount");
        Object result = getAccountImpl.handle(req, res);

        Assert.assertEquals(expectedResult, result);
        Mockito.verify(res, Mockito.times(1)).status(HttpStatus.OK_200);
    }

    @Test(expected = ApplicationException.class)
    public void testGetAccountsByIncorrectIdWithApplicationException() throws Exception {
        ApplicationException ex = new ApplicationException();
        when(RequestUtils.getIdFromRequest(req, RouteConstants.Params.ACCOUNT_ID)).thenThrow(ex);

        Route getAccountImpl = Whitebox.getInternalState(accountController, "getAccount");

        try {
            getAccountImpl.handle(req, res);
        } catch (DataBaseException e) {
            Assert.assertEquals(ex, e);
            Mockito.verify(res, Mockito.times(0)).status(HttpStatus.OK_200);
            throw e;
        }
    }

    @Test(expected = NotFoundException.class)
    public void testGetAccountsByIdWithNotFoundException() throws Exception {
        when(RequestUtils.getIdFromRequest(req, RouteConstants.Params.ACCOUNT_ID)).thenReturn(accountId);
        when(accountServiceMock.getAccount(accountId)).thenReturn(null);

        Route getAllAccountsImpl = Whitebox.getInternalState(accountController, "getAccount");

        try {
            getAllAccountsImpl.handle(req, res);
        } catch (DataBaseException e) {
            Assert.assertEquals(ERROR_COULD_NOT_FIND_ACCOUNT, e.getMessage());
            Mockito.verify(res, Mockito.times(0)).status(HttpStatus.OK_200);
            throw e;
        }
    }

    @Test(expected = DataBaseException.class)
    public void testGetAccountsByIdWithDbException() throws Exception {
        when(RequestUtils.getIdFromRequest(req, RouteConstants.Params.ACCOUNT_ID)).thenReturn(accountId);
        DataBaseException ex = new DataBaseException();
        when(accountServiceMock.getAccount(accountId)).thenThrow(ex);

        Route getAllAccountsImpl = Whitebox.getInternalState(accountController, "getAccount");

        try {
            getAllAccountsImpl.handle(req, res);
        } catch (DataBaseException e) {
            Assert.assertEquals(ex, e);
            Mockito.verify(res, Mockito.times(0)).status(HttpStatus.OK_200);
            throw e;
        }
    }
}
