package api.response.apiResponse.Exceptions;

public class ConvertToAddressEntityException extends RuntimeException {
    public ConvertToAddressEntityException(String message) {
        super(message);
    }

    public ConvertToAddressEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
