package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;

public interface PageProcessorService {
    void processPage(long page);
    GetAllAddressesResponse getAddressResponse(String url, long page);
}
