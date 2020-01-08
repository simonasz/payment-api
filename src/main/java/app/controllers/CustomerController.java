package app.controllers;

import app.constants.RouteConstants;
import app.exceptions.NotFoundException;
import app.exceptions.NotImplementedException;
import app.models.Account;
import app.models.Customer;
import app.models.validators.CustomerValidator;
import app.services.AccountService;
import app.services.CustomerService;
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

import static app.constants.ErrorMessages.ERROR_COULD_NOT_FIND_CUSTOMER;
import static spark.Spark.*;

/**
 * Customer controller
 */
public class CustomerController implements RouteGroup {
    @Inject
    private CustomerService customerService;
    @Inject
    private AccountService accountService;

    /**
     * Add CustomerController routes
     */
    @Override
    public void addRoutes() {
        get(RouteConstants.Path.EMPTY, this.getAllCustomers);
        get(RouteConstants.Path.SEPARATOR + RouteConstants.Params.CUSTOMER_ID, this.getCustomer);
        post(RouteConstants.Path.EMPTY, "application/json", this.createCustomer);
        patch(RouteConstants.Path.SEPARATOR + RouteConstants.Params.CUSTOMER_ID, "application/json",
                this.updateCustomer);
        delete(RouteConstants.Path.SEPARATOR + RouteConstants.Params.CUSTOMER_ID, this.deleteCustomer);
        get(RouteConstants.Path.SEPARATOR + RouteConstants.Params.CUSTOMER_ID + RouteConstants.Path.ACCOUNT,
                this.getCustomerAccounts);
    }

    /**
     * Get all customers
     */
    private Route getAllCustomers = (Request req, Response res) -> {
        List<Customer> customers = this.customerService.getCustomers();
        return ObjectTransformer.objectToString(customers);
    };

    /**
     * Get customer by id
     */
    private Route getCustomer = (Request req, Response res) -> {
        Long customerId = RequestUtils.getIdFromRequest(req, RouteConstants.Params.CUSTOMER_ID);

        Optional<Customer> result = Optional.ofNullable(customerService.getCustomer(customerId));
        if (!result.isPresent())
            throw new NotFoundException(ERROR_COULD_NOT_FIND_CUSTOMER);

        res.status(HttpStatus.OK_200);
        return ObjectTransformer.objectToString(result.get());
    };

    /**
     * Create new customer
     */
    private Route createCustomer = (Request req, Response res) -> {
        Customer customer = ObjectTransformer.getRequestObject(req.body(), Customer.class);
        CustomerValidator.validateCustomerData(customer);
        customer = customerService.createCustomer(customer);
        res.status(HttpStatus.CREATED_201);
        return ObjectTransformer.objectToString(customer);
    };

    /**
     * Update existing customer
     */
    private Route updateCustomer = (Request req, Response res) -> {
        Long customerId = RequestUtils.getIdFromRequest(req, RouteConstants.Params.CUSTOMER_ID);

        Optional<Customer> existingCustomer = Optional.ofNullable(customerService.getCustomer(customerId));
        if (!existingCustomer.isPresent())
            throw new NotFoundException(String.format("Could not find customer with id: %s", customerId));

        Customer newCustomer = ObjectTransformer.getRequestObject(req.body(), Customer.class);
        newCustomer.setId(existingCustomer.get().getId());
        if (newCustomer.getFirstName() == null)
            newCustomer.setFirstName(existingCustomer.get().getFirstName());
        if (newCustomer.getLastName() == null)
            newCustomer.setLastName(existingCustomer.get().getLastName());

        CustomerValidator.validateCustomerData(newCustomer);

        newCustomer = customerService.updateCustomer(newCustomer);
        res.status(HttpStatus.OK_200);
        return ObjectTransformer.objectToString(newCustomer);
    };

    /**
     * Delete customer
     * Not implemented
     */
    private Route deleteCustomer = (Request req, Response res) -> {
        throw new NotImplementedException();
    };

    /**
     * Get customer accounts
     */
    private Route getCustomerAccounts = (Request req, Response res) -> {
        Long customerId = RequestUtils.getIdFromRequest(req, RouteConstants.Params.CUSTOMER_ID);

        Optional<Customer> customer = Optional.ofNullable(customerService.getCustomer(customerId));
        if (!customer.isPresent())
            throw new NotFoundException(ERROR_COULD_NOT_FIND_CUSTOMER);

        List<Account> result = accountService.getAccountsByCustomerId(customerId);

        res.status(HttpStatus.OK_200);
        return ObjectTransformer.objectToString(result);
    };
}
