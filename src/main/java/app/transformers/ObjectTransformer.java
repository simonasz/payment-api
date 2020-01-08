package app.transformers;

import app.exceptions.InvalidRequestData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Object transformer to transform objects to/from json based string
 */
public class ObjectTransformer {

    /**
     * Get object from Request body
     *
     * @param content
     * @param typeParameterClass
     * @param <T>
     * @return
     * @throws InvalidRequestData
     */
    public static <T> T getRequestObject(String content, Class<T> typeParameterClass) throws InvalidRequestData {
        try {
            return getObject(content, typeParameterClass);
        } catch (IOException e) {
            throw new InvalidRequestData("Invalid request data");
        }
    }

    /**
     * Get object from json string
     *
     * @param content
     * @param typeParameterClass
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T getObject(String content, Class<T> typeParameterClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(content, typeParameterClass);
    }

    /**
     * Create json string from object
     *
     * @param obj
     * @return
     */
    public static String objectToString(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, obj);
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Create json string for Exception object
     *
     * @param ex
     * @return
     */
    public static String exceptionToString(Exception ex) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, new ErrorResponse(ex.getMessage()));
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Inner class used for error response mapping
     */
    @NoArgsConstructor
    public static class ErrorResponse {
        @Getter
        @Setter
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
