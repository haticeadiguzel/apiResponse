package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.entities.concretes.Address;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface AddressService {
    void fetchAddressesData();

    Address convertToAddressEntity(GetAllAddressesResponse addressResponse);

    void processPage(long page);

    List<Map<String, String>> defaultListUrl();

    void crunchifyWhois(String url);

    Map<String, String> getWhoisResultFromRedis(String url);

    List<Map<String, String>> getWhoisListResultFromRedis(List<String> urls);

    long getTotalCountOfUrl(String url);

    GetAllAddressesResponse getAddressResponse(String url, long page);

    String getWhoisResultFromWhoisApi(String url);

    JSONObject parseWhoisResult(String whoisResultString, String url);

    void saveRedis(JSONObject jsonWhois);

    void saveDB(Address addressEntity);

    void runCrunchifyWhoisToAllUrl(Address addressEntity);

}
