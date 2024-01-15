package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.entities.concretes.Address;
import org.json.JSONObject;

public interface WhoisRunnerService {
    void runCrunchifyWhoisToAllUrl(Address addressEntity);

    void crunchifyWhois(String url);

    String getWhoisResultFromWhoisApi(String url);

    JSONObject parseWhoisResult(String whoisResultString, String url);
}
