package api.response.apiResponse.Exceptions;

public class GetAllAddressesResponseException extends RuntimeException{
    public GetAllAddressesResponseException(String message) {
        super(message);
    }

    public GetAllAddressesResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
