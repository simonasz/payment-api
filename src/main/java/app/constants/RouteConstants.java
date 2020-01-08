package app.constants;

/**
 * Spark server route constants
 */
public class RouteConstants {
    /**
     * Path route constants
     */
    public static class Path {
        public final static String EMPTY = "";
        public final static String SEPARATOR = "/";
        public final static String ACCOUNT = "/account";
        public final static String CUSTOMER = "/customer";
        public final static String TRANSACTION = "/transaction";
        public final static String HEALTH = "/health";
    }

    /**
     * Path parameter constants
     */
    public static class Params {
        public final static String CUSTOMER_ID = ":cid";
        public final static String ACCOUNT_ID = ":aid";
        public final static String TRANSACTION_ID = ":tid";
    }
}
