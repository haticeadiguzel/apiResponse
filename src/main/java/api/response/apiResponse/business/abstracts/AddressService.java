package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.entities.concretes.Address;
import org.json.JSONObject;

import java.util.List;

public interface AddressService {
    void getAddressesData();
    Address convertToAddressEntity(GetAllAddressesResponse addressResponse);
    void crunchifyWhois(String url);
    String getWhois(String url);
    long getTotalCount(String url);
    GetAllAddressesResponse getAddressResponse(String url, long page);
    void processPage(List<GetAllAddressesResponse> responses, long page);
    void handleTooManyRequests();
}
