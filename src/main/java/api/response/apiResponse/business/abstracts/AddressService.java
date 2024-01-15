package api.response.apiResponse.business.abstracts;

import java.util.List;
import java.util.Map;

public interface AddressService {
    void fetchAddressesData();

    List<Map<String, String>> getWhoisListResultFromRedis(List<String> urls);

    long getTotalCountOfUrl(String url);
}
