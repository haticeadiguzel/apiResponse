package api.response.apiResponse.business.concretes;

import api.response.apiResponse.Exceptions.ParseWhoisResultException;
import api.response.apiResponse.Exceptions.WhoisResultRequestException;
import api.response.apiResponse.business.abstracts.RedisService;
import api.response.apiResponse.business.abstracts.WhoisRunnerService;
import api.response.apiResponse.entities.concretes.Address;
import api.response.apiResponse.entities.concretes.Model;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.whois.WhoisClient;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class WhoisRunnerManager implements WhoisRunnerService {
    final private RedisService redisService;

    public WhoisRunnerManager(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public void runCrunchifyWhoisToAllUrl(Address addressEntity) {
        try {
            List<Model> models = addressEntity.getModels();
            for (Model model : models) {
                String url = model.getUrl();
                crunchifyWhois(url);
            }
        } catch (Exception e) {
            log.error("Error run crunchify whois to all urls: ", e);
        }
    }

    @Override
    public void crunchifyWhois(String url) {
        String whoisResultString = getWhoisResultFromWhoisApi(url);
        JSONObject jsonWhois = parseWhoisResult(whoisResultString, url);
        redisService.saveRedis(jsonWhois);
        System.out.println("Whois data processed for URL: " + url);
    }

    @Override
    public String getWhoisResultFromWhoisApi(String url) {
        try {
            StringBuilder whoisResult = new StringBuilder("");
            WhoisClient crunchifyWhois = new WhoisClient();
            crunchifyWhois.connect("whois.verisign-grs.com");
            String whoisData = crunchifyWhois.query("=" + url);
            whoisResult.append(whoisData);
            int lastIndexWhois = whoisResult.lastIndexOf(">>>");
            int length = whoisResult.length();
            whoisResult.delete(lastIndexWhois, length);
            crunchifyWhois.disconnect();
            return whoisResult.toString();
        } catch (IOException e) {
            throw new WhoisResultRequestException("Error occurred while getting whois result from whois api: ", e);
        }
    }

    @Override
    public JSONObject parseWhoisResult(@NotNull String whoisResultString, String url) {
        try {
            JSONObject jsonWhois = new JSONObject();
            String currentKey = null;
            StringBuilder currentValue = new StringBuilder();
            String[] lines = whoisResultString.split("\n");

            for (String line : lines) {
                String[] fields = line.split(": ", 2);
                if (fields.length == 2) {
                    String key = fields[0].trim();
                    String value = fields[1].trim();

                    if (key.equals(currentKey)) {
                        currentValue.append(", ").append(value);
                    } else {
                        if (currentKey != null) {
                            jsonWhois.put(currentKey, currentValue.toString());
                        }
                        currentKey = key;
                        currentValue = new StringBuilder(value);
                    }
                }
            }
            if (currentKey != null) {
                jsonWhois.put(currentKey, currentValue.toString());
            }
            jsonWhois.put("URL", url);
            return jsonWhois;
        } catch (Exception e) {
            throw new ParseWhoisResultException("Error occurred while parsing whois result from whois api: ", e);
        }
    }
}
