package test;

import app.constants.RouteConstants;
import app.models.Account;
import app.models.Customer;
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static app.constants.ErrorMessages.*;

public class CustomerFunctionalTest {
    private static final Long nonExistingCustomerId = Long.valueOf(-155478);
    private static String rootPathUrl;
    private TestData testData = new TestData();

    public CustomerFunctionalTest() throws SQLException {
    }

    @Before
    public void beforeEach() throws SQLException {
        rootPathUrl = "http://127.0.0.1:" + spark.Spark.port() + RouteConstants.Path.CUSTOMER;
        testData.deleteTestData();
    }

    @Test
    public void shouldTryToGetAllCustomers() throws IOException, SQLException {
        testData.createTestCustomers();
        List<Customer> existingCustomers = testData.getTestCustomers();

        //check customers exist
        Assert.assertNotEquals(0, existingCustomers.size());

        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl);

        //Verify new created customer
        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());
        List<Customer> receivedCustomers = Arrays.asList(ObjectTransformer.getObject(apiClientResult.getResult(),
                Customer[].class));

        //Verify against database
        Assert.assertEquals(existingCustomers.size(), receivedCustomers.size());
        existingCustomers.forEach(c -> {
            Optional<Customer> rcOpt = receivedCustomers.stream().filter(rc -> c.equals(rc)).findFirst();
            Assert.assertNotNull(rcOpt.get());
            Validators.verifyCustomerEqualsFull(c, rcOpt.get());
        });
    }

    @Test
    public void shouldTryToGetCustomerById() throws IOException, SQLException {
        testData.createTestCustomers();
        List<Customer> existingCustomers = testData.getTestCustomers();

        Assert.assertNotEquals(0, existingCustomers.size());

        Customer customerToFind = existingCustomers.get(0);

        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl + "/" + customerToFind.getId());

        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());

        //Validate against database
        Customer receivedCustomer = ObjectTransformer.getObject(apiClientResult.getResult(), Customer.class);
        Assert.assertNotNull(receivedCustomer);
        Assert.assertEquals(customerToFind, receivedCustomer);
        Validators.verifyCustomerEqualsFull(customerToFind, receivedCustomer);
    }

    @Test
    public void shouldTryToGetNonExistingCustomerById() throws IOException, SQLException {
        testData.createTestCustomers();
        List<Customer> existingCustomers = testData.getTestCustomers();

        Assert.assertNotEquals(0, existingCustomers.size());

        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl + "/" + nonExistingCustomerId);
        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);

        //Validate error response content
        Assert.assertEquals(HttpStatus.NOT_FOUND_404, apiClientResult.getStatus());
        Assert.assertEquals(ERROR_COULD_NOT_FIND_CUSTOMER, errorResponse.getError());
    }

    @Test
    public void shouldTryToCreateCustomer() throws IOException, SQLException {
        List<Customer> existingCustomers = testData.getTestCustomers();
        Assert.assertEquals(0, existingCustomers.size());

        Customer customerReq = new Customer();
        customerReq.setFirstName("Thomas");
        customerReq.setLastName("Fin");

        ApiClient.ApiClientResult apiClientResult = ApiClient.post(rootPathUrl, customerReq);

        //Verify new created customer
        Assert.assertEquals(HttpStatus.CREATED_201, apiClientResult.getStatus());
        Customer newCustomer = ObjectTransformer.getObject(apiClientResult.getResult(), Customer.class);
        Validators.verifyCustomerEquals(customerReq, newCustomer);

        existingCustomers = testData.getTestCustomers();
        Assert.assertEquals(1, existingCustomers.size());
        Assert.assertEquals(newCustomer, existingCustomers.get(0));
    }

    @Test
    public void shouldTryToCreateCustomerWithInvalidFirstName() throws IOException, SQLException {
        List<Customer> existingCustomers = testData.getTestCustomers();
        Assert.assertEquals(0, existingCustomers.size());

        Customer customer = new Customer();
        customer.setFirstName("");
        customer.setLastName("Fin");

        ApiClient.ApiClientResult apiClientResult = ApiClient.post(rootPathUrl, customer);

        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);

        //Validate error response content
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, apiClientResult.getStatus());
        Assert.assertEquals(ERROR_INVALID_PROPERTY_FIRSTNAME, errorResponse.getError());

        //Validate none customers were created
        existingCustomers = testData.getTestCustomers();
        Assert.assertEquals(0, existingCustomers.size());
    }

    @Test
    public void shouldTryToCreateCustomerWithInvalidLastName() throws IOException, SQLException {
        List<Customer> existingCustomers = testData.getTestCustomers();
        Assert.assertEquals(0, existingCustomers.size());

        Customer customer = new Customer();
        customer.setFirstName("Tom");
        customer.setLastName("");

        ApiClient.ApiClientResult apiClientResult = ApiClient.post(rootPathUrl, customer);

        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);

        //Validate error response content
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, apiClientResult.getStatus());
        Assert.assertEquals(ERROR_INVALID_PROPERTY_LASTNAME, errorResponse.getError());

        //Validate none customers were created
        existingCustomers = testData.getTestCustomers();
        Assert.assertEquals(0, existingCustomers.size());
    }

    @Test
    public void shouldTryToUpdateCustomer() throws IOException, SQLException {
        testData.createTestCustomers();
        List<Customer> existingCustomers = testData.getTestCustomers();

        Customer existingCustomerToUpdate = existingCustomers.get(0);

        String newFirstName = "Tony";
        String newLastName = "Cameron";
        Timestamp newCreatedDate = new Timestamp(System.currentTimeMillis() - 1000);

        Customer updateCustomerReq = new Customer();
        updateCustomerReq.setId(existingCustomerToUpdate.getId());
        updateCustomerReq.setFirstName(newFirstName);
        updateCustomerReq.setLastName(newLastName);
        updateCustomerReq.setCreated(newCreatedDate);

        ApiClient.ApiClientResult apiClientResult =
                ApiClient.patch(rootPathUrl + "/" + existingCustomerToUpdate.getId(), updateCustomerReq);

        //Verify updated customer
        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());
        Customer updatedCustomer = ObjectTransformer.getObject(apiClientResult.getResult(), Customer.class);
        Validators.verifyCustomerEquals(updateCustomerReq, updatedCustomer);

        //Verify created date was not updated
        Assert.assertNotEquals(newCreatedDate, updatedCustomer.getCreated());
        Assert.assertEquals(existingCustomerToUpdate.getCreated(), updatedCustomer.getCreated());

        //Verify against database
        existingCustomers = testData.getTestCustomers();
        Optional<Customer> originalCustomer =
                existingCustomers.stream().filter(c -> c.getId().equals(existingCustomerToUpdate.getId())).findFirst();
        Assert.assertNotNull(originalCustomer.get());
        Validators.verifyCustomerEquals(originalCustomer.get(), updatedCustomer);
    }

    @Test
    public void shouldTryToUpdateCustomerWithIncorrectFirstName() throws IOException, SQLException {
        testData.createTestCustomers();
        List<Customer> existingCustomers = testData.getTestCustomers();

        Customer existingCustomerToUpdate = existingCustomers.get(0);

        String newFirstName = "";
        String newLastName = "Cameron";
        Timestamp newCreatedDate = new Timestamp(System.currentTimeMillis());

        Customer updateCustomerReq = new Customer();
        updateCustomerReq.setId(existingCustomerToUpdate.getId());
        updateCustomerReq.setFirstName(newFirstName);
        updateCustomerReq.setLastName(newLastName);
        updateCustomerReq.setCreated(newCreatedDate);

        ApiClient.ApiClientResult apiClientResult =
                ApiClient.patch(rootPathUrl + "/" + existingCustomerToUpdate.getId(), updateCustomerReq);

        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);
        //Validate error response content
        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, apiClientResult.getStatus());
        Assert.assertEquals(ERROR_INVALID_PROPERTY_FIRSTNAME, errorResponse.getError());

        //Verify against database that customer was not updated
        existingCustomers = testData.getTestCustomers();
        Optional<Customer> originalCustomer =
                existingCustomers.stream().filter(c -> c.getId().equals(existingCustomerToUpdate.getId())).findFirst();
        Assert.assertNotNull(originalCustomer.get());
        Validators.verifyCustomerEqualsFull(originalCustomer.get(), existingCustomerToUpdate);
    }

    @Ignore
    @Test
    public void shouldTryToUpdateCustomerWithIncorrectLastName() {
    }

    @Test
    public void shouldTryToDeleteCustomerById() throws IOException, SQLException {
        testData.createTestCustomers();
        List<Customer> existingCustomers = testData.getTestCustomers();

        Customer existingCustomerToDelete = existingCustomers.get(0);

        ApiClient.ApiClientResult apiClientResult =
                ApiClient.delete(rootPathUrl + "/" + existingCustomerToDelete.getId());

        Assert.assertEquals(HttpStatus.NOT_IMPLEMENTED_501, apiClientResult.getStatus());

        //Verify against database that customer was not deleted
        existingCustomers = testData.getTestCustomers();
        Optional<Customer> originalCustomer =
                existingCustomers.stream().filter(c -> c.getId().equals(existingCustomerToDelete.getId())).findFirst();
        Assert.assertNotNull(originalCustomer.get());
        Validators.verifyCustomerEqualsFull(existingCustomerToDelete, originalCustomer.get());
    }

    @Test
    public void shouldTryToGetListOfAccountsByCustomerId() throws IOException, SQLException {
        testData.createTestAccounts();
        List<Customer> existingCustomers = testData.getTestCustomers();
        Customer customer = existingCustomers.get(0);

        List<Account> existingAccounts = testData.getTestAccounts();
        List<Account> customerAccounts =
                existingAccounts.stream().filter(a -> a.getCustomerId().equals(customer.getId())).collect(Collectors.toList());
        Assert.assertNotEquals(0, customerAccounts.size());

        ApiClient.ApiClientResult apiClientResult =
                ApiClient.get(rootPathUrl + "/" + customer.getId() + RouteConstants.Path.ACCOUNT);

        //Verify new created customer
        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());
        List<Account> receivedAccounts = Arrays.asList(ObjectTransformer.getObject(apiClientResult.getResult(),
                Account[].class));

        Assert.assertEquals(customerAccounts.size(), receivedAccounts.size());
        customerAccounts.forEach(ca -> {
            Optional<Account> raOpt = receivedAccounts.stream().filter(rc -> ca.equals(rc)).findFirst();
            Assert.assertNotNull(raOpt.get());
            Validators.verifyAccountEqualsFull(ca, raOpt.get());
        });
    }

    @Ignore
    @Test
    public void shouldTryToGetListOfAccountsByNonExistingCustomerId() {
    }
}
