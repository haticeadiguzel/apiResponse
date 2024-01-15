package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.entities.concretes.Address;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface AddressService {
    void fetchAddressesData();

    List<Map<String, String>> getWhoisListResultFromRedis(List<String> urls);

    long getTotalCountOfUrl(String url);
}
