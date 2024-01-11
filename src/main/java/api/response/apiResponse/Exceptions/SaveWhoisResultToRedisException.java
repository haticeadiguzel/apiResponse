package api.response.apiResponse.Exceptions;

public class SaveWhoisResultToRedisException extends RuntimeException {
    public SaveWhoisResultToRedisException(String message) {
        super(message);
    }

    public SaveWhoisResultToRedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
