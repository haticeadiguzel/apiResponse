package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.entities.concretes.Address;

public interface PageProcessorService {
    void processPage(long page);
    GetAllAddressesResponse getAddressResponse(String url, long page);
}
