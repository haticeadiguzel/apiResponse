package api.response.apiResponse.Exceptions;

public class GetTotalCountException extends RuntimeException {
    public GetTotalCountException(String message) {
        super(message);
    }

    public GetTotalCountException(String message, Throwable cause) {
        super(message, cause);
    }
}
