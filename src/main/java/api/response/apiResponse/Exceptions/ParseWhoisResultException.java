package api.response.apiResponse.Exceptions;

public class ParseWhoisResultException extends RuntimeException {
    public ParseWhoisResultException(String message) {
        super(message);
    }

    public ParseWhoisResultException(String message, Throwable cause) {
        super(message, cause);
    }
}
