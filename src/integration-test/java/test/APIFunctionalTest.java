package test;

import app.constants.RouteConstants;
import app.transformers.ObjectTransformer;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import utils.ApiClient;

import java.io.IOException;

import static app.constants.ErrorMessages.ERROR_NOT_FOUND;

public class APIFunctionalTest {
    private static String rootPathUrl;

    @Before
    public void beforeEach() {
        rootPathUrl = "http://127.0.0.1:" + spark.Spark.port();
    }

    @Test
    public void shouldTryToGetHealth() throws IOException {
        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl + RouteConstants.Path.HEALTH);
        Assert.assertEquals(HttpStatus.OK_200, apiClientResult.getStatus());
    }

    @Test
    public void shouldTryToGetUnknownRoute() throws IOException {
        String unknownRoute = "/unknown_route";
        ApiClient.ApiClientResult apiClientResult = ApiClient.get(rootPathUrl + unknownRoute);
        ObjectTransformer.ErrorResponse errorResponse = ObjectTransformer.getObject(apiClientResult.getResult(),
                ObjectTransformer.ErrorResponse.class);

        Assert.assertEquals(HttpStatus.NOT_FOUND_404, apiClientResult.getStatus());
        Assert.assertEquals(ERROR_NOT_FOUND, errorResponse.getError());
    }
}
