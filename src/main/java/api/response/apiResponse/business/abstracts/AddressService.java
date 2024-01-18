package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.WhoisResultResponse;

import java.util.List;

public interface AddressService {
    void fetchAddressesData();

    List<WhoisResultResponse> getWhoisListResultFromRedis(List<String> urls);

    long getTotalCountOfUrl(String url);
}
