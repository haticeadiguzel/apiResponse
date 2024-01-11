package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;

import java.util.List;

public interface AddressFetcherService {
    List<GetAllAddressesResponse> fetchAddressesData(String url);
}
