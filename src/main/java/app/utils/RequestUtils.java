package app.utils;

import app.exceptions.ApplicationException;
import spark.Request;

/**
 * Utility class for processing requests
 */
public class RequestUtils {
    /**
     * Extract parameter from request path of type Long
     *
     * @param req
     * @param param
     * @return
     * @throws ApplicationException
     */
    public static Long getIdFromRequest(Request req, String param) throws ApplicationException {
        try {
            return Long.valueOf(req.params(param));
        } catch (NumberFormatException e) {
            throw new ApplicationException("Invalid id");
        }
    }
}
