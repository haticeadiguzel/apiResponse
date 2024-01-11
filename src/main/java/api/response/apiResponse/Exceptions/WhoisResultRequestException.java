package api.response.apiResponse.Exceptions;

public class WhoisResultRequestException extends RuntimeException{
    public WhoisResultRequestException(String message) {
        super(message);
    }

    public WhoisResultRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}