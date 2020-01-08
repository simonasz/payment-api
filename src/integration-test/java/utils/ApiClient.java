package utils;

import app.transformers.ObjectTransformer;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Helper class for REST API calls
 */
public class ApiClient {
    private static Logger logger = LoggerFactory.getLogger(ApiClient.class);

    /**
     * ApiClient result class
     */
    public static class ApiClientResult {
        private int status;
        private String result;

        public ApiClientResult(int status, String result) {
            this.status = status;
            this.result = result;
        }

        public int getStatus() {
            return status;
        }

        public String getResult() {
            return result;
        }
    }

    /**
     * GET operation
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static ApiClientResult get(String url) throws IOException {
        try {
            HttpResponse httpResponse =
                    Request.Get(url).connectTimeout(3000).socketTimeout(3000).execute().returnResponse();

            ApiClientResult apiClientResult = new ApiClientResult(
                    httpResponse.getStatusLine().getStatusCode(),
                    EntityUtils.toString(httpResponse.getEntity())
            );
            return apiClientResult;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * POST operation
     *
     * @param url
     * @param objectToSend
     * @return
     * @throws IOException
     */
    public static ApiClientResult post(String url, Object objectToSend) throws IOException {
        try {
            String json = ObjectTransformer.objectToString(objectToSend);
            HttpResponse httpResponse = Request.Post(url).connectTimeout(3000).socketTimeout(3000).bodyString(json,
                    ContentType.APPLICATION_JSON).execute().returnResponse();

            ApiClientResult apiClientResult = new ApiClientResult(
                    httpResponse.getStatusLine().getStatusCode(),
                    EntityUtils.toString(httpResponse.getEntity())
            );
            return apiClientResult;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * PATCH operation
     *
     * @param url
     * @param objectToSend
     * @return
     * @throws IOException
     */
    public static ApiClientResult patch(String url, Object objectToSend) throws IOException {
        try {
            String json = ObjectTransformer.objectToString(objectToSend);
            HttpResponse httpResponse = Request.Patch(url).connectTimeout(3000).socketTimeout(3000).bodyString(json,
                    ContentType.APPLICATION_JSON).execute().returnResponse();

            ApiClientResult apiClientResult = new ApiClientResult(
                    httpResponse.getStatusLine().getStatusCode(),
                    EntityUtils.toString(httpResponse.getEntity())
            );
            return apiClientResult;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Delete operation
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static ApiClientResult delete(String url) throws IOException {
        try {
            HttpResponse httpResponse =
                    Request.Delete(url).connectTimeout(3000).socketTimeout(3000).execute().returnResponse();

            ApiClientResult apiClientResult = new ApiClientResult(
                    httpResponse.getStatusLine().getStatusCode(),
                    EntityUtils.toString(httpResponse.getEntity())
            );
            return apiClientResult;
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
}
