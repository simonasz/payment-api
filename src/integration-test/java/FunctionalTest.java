import app.Application;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import spark.Spark;
import test.APIFunctionalTest;
import test.AccountFunctionalTest;
import test.CustomerFunctionalTest;
import test.TransactionFunctionalTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({APIFunctionalTest.class, AccountFunctionalTest.class, CustomerFunctionalTest.class,
        TransactionFunctionalTest.class})
public class FunctionalTest {
    @BeforeClass
    public static void setUp() throws InterruptedException {
        String[] args = {};
        Application.main(args);
        Spark.awaitInitialization();
        Thread.sleep(3000); //Allow Spark to initialize
    }

    @AfterClass
    public static void tearDown() {
        Spark.stop();
        Spark.awaitStop();
    }
}
