package api.response.apiResponse.Exceptions;

public class SaveToDbException extends RuntimeException {
    public SaveToDbException(String message) {
        super(message);
    }

    public SaveToDbException(String message, Throwable cause) {
        super(message, cause);
    }
}
