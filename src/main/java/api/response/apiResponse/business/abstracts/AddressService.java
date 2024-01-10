package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.entities.concretes.Address;

import java.util.List;

public interface AddressService {
    void fetchAddressesData();

    Address convertToAddressEntity(GetAllAddressesResponse addressResponse);

    void processPage(List<GetAllAddressesResponse> responses, long page);

    void crunchifyWhois(String url);

    String getWhois(String url);

    long getTotalCount(String url);

    GetAllAddressesResponse getAddressResponse(String url, long page);
}
