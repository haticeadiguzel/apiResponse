package api.response.apiResponse.Exceptions;

public class TooManyGetRequestException extends RuntimeException {
    public TooManyGetRequestException(String message) {
        super(message);
    }

    public TooManyGetRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
