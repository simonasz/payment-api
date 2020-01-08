package app;

import app.constants.RouteConstants;
import app.controllers.AccountController;
import app.controllers.CustomerController;
import app.controllers.TransactionController;
import app.db.DataSource;
import app.exceptions.PaymentAPIException;
import app.transformers.ObjectTransformer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static app.constants.ErrorMessages.ERROR_INTERNAL_SERVER_ERROR;
import static app.constants.ErrorMessages.ERROR_NOT_FOUND;
import static spark.Spark.*;

/**
 * Main class
 */
public class Application {
    private static final String VAR_PORT = "sparkPort";
    private static final int DEFAULT_PORT = 3000;
    private static Logger logger = LoggerFactory.getLogger(Application.class);

    /**
     * Main static method to run application
     *
     * @param args
     */
    public static void main(String[] args) {
        port(getSparkPort());

        //Enable multi threads
        int maxThreads = 8;
        int minThreads = 2;
        int timeOutMillis = 30000;
        threadPool(maxThreads, minThreads, timeOutMillis);

        Injector injector = Guice.createInjector();
        DataSource dataSource = injector.getInstance(DataSource.class);
        dataSource.cleanDB();

        before("/*", (req, res) -> {
            logger.debug(req.toString());
        });
        //Define paths
        path(RouteConstants.Path.ACCOUNT, injector.getInstance(AccountController.class));
        path(RouteConstants.Path.CUSTOMER, injector.getInstance(CustomerController.class));
        path(RouteConstants.Path.TRANSACTION, injector.getInstance(TransactionController.class));

        //Health endpoint
        get(RouteConstants.Path.HEALTH, (req, res) -> {
            res.status(HttpStatus.OK_200);
            return "{\"status\":\"OK\"}";
        });

        //Handle exceptions
        exception(PaymentAPIException.class, (e, req, res) -> {
            logger.warn(e.getMessage());
            res.status(e.getStatus());
            res.body(ObjectTransformer.exceptionToString(e));
        });
        exception(Exception.class, (e, req, res) -> {
            logger.warn(e.getMessage());
            res.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            res.body(ObjectTransformer.exceptionToString(e));
        });
        internalServerError((req, res) -> {
            logger.warn("Internal server error");
            return String.format("{\"error\":\"%s\"}", ERROR_INTERNAL_SERVER_ERROR);
        });
        notFound((req, res) -> {
            logger.warn("Route not found");
            return String.format("{\"error\":\"%s\"}", ERROR_NOT_FOUND);
        });
        after("/*", (req, res) -> {
            logger.debug(res.body());
            res.type("application/json");
        });
    }

    /**
     * Get Spark server port
     *
     * @return
     */
    private static int getSparkPort() {
        try {
            Optional<String> sparkPortOpt = Optional.ofNullable(System.getProperty(VAR_PORT));
            if (sparkPortOpt.isPresent())
                return Integer.valueOf(sparkPortOpt.get());
        } catch (NumberFormatException e) {
            logger.warn("Invalid sparkPort provided. Will run on default 3000");
        }
        return DEFAULT_PORT;
    }
}
