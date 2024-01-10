package api.response.apiResponse.business.concretes;

import api.response.apiResponse.Exceptions.ApiRequestException;
import api.response.apiResponse.Exceptions.TooManyGetRequestException;
import api.response.apiResponse.Logger.Logger;
import api.response.apiResponse.Logger.Utils;
import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.AddressService;
import api.response.apiResponse.core.utilities.mappers.ModelMapperService;
import api.response.apiResponse.dataAccess.abstracts.AddressRepository;
import api.response.apiResponse.dataAccess.abstracts.RedisRepository;
import api.response.apiResponse.entities.concretes.Address;
import api.response.apiResponse.entities.concretes.Model;
import api.response.apiResponse.entities.concretes.Whois;
import org.apache.commons.net.whois.WhoisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
@Slf4j
@EnableCaching
public class AddressManager implements AddressService {
    final private AddressRepository addressRepository;
    final private ModelMapperService modelMapperService;
    final private RedisRepository redisRepository;
    final private Logger[] loggers;
    @Value("${address.url}")
    private String urlAPI;
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    public AddressManager(AddressRepository addressRepository, ModelMapperService modelMapperService, RedisRepository redisRepository, Logger[] loggers) {
        this.addressRepository = addressRepository;
        this.modelMapperService = modelMapperService;
        this.redisRepository = redisRepository;
        this.loggers = loggers;
    }

    @Scheduled(initialDelay = 1000, fixedRate = 21600000)
    @Override
    public void fetchAddressesData() {
        List<GetAllAddressesResponse> responses = new ArrayList<>();
        long totalCount = getTotalCount(urlAPI);
        long pageCount = totalCount / 1550 + 1;
        System.out.println(pageCount);
        try {
            for (long page = 0; page < pageCount; page++) {
                processPage(responses, page);
            }
        }
        catch (Exception e) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException exception) {
                throw new TooManyGetRequestException("Too many get request from api...", exception);
            } catch (WebClientResponseException exception) {
                throw new ApiRequestException("Per-page value is bigger than 1550...", exception);
            }
        }
    }

    @Override
    public String getWhois(String url) {
        try (Jedis jedis = new Jedis(host, port);) {
            Map<String, String> result = jedis.hgetAll("whoises:" + url);
            for (Map.Entry<String, String> entry : result.entrySet()) {
                try {
                    if (entry.getKey().equals("registryExpiryDate")) {
                        log.info("Field: " + entry.getKey() + ", Value: " + entry.getValue());
                        return entry.getValue();
                    }
                } catch (Exception e) {
                    log.info("Error occured when getting data from redis...");
                }
            }
            return result.toString();
        } catch (Exception e) {
            return "Error: When connect to redis...";
        }
    }

    @Override
    public Address convertToAddressEntity(GetAllAddressesResponse addressResponse) {
        return this.modelMapperService.forResponse().map(addressResponse, Address.class);
    }

    @Override
    public void crunchifyWhois(String url) {
        StringBuilder whoisResult = new StringBuilder("");
        WhoisClient crunchifyWhois = new WhoisClient();
        try {
            crunchifyWhois.connect("whois.verisign-grs.com");
            String whoisData = crunchifyWhois.query("=" + url);
            whoisResult.append(whoisData);
            int lastIndexWhois = whoisResult.lastIndexOf(">>>");
            int length = whoisResult.length();
            whoisResult.delete(lastIndexWhois, length);
            String whoisResultString = whoisResult.toString();
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
            Whois whois = new Gson().fromJson(jsonWhois.toString(), Whois.class);
            System.out.println(url);
            redisRepository.save(whois);
            crunchifyWhois.disconnect();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public long getTotalCount(String url) {
        WebClient.Builder builderPageCount = WebClient.builder();
        GetAllAddressesResponse addressTotalCount = builderPageCount.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(GetAllAddressesResponse.class)
                .block();
        Address addressEntityTotalCount = convertToAddressEntity(addressTotalCount);
        return addressEntityTotalCount.getTotalCount();
    }

    @Override
    public GetAllAddressesResponse getAddressResponse(String url, long page) {
        WebClient.Builder builder = WebClient.builder();
        WebClient webClient = builder.baseUrl(url).build();
        String pageUrl = String.format("?page=%d&per-page=1550", page);
        return webClient
                .get()
                .uri(pageUrl)
                .retrieve()
                .bodyToMono(GetAllAddressesResponse.class)
                .block();
    }

    @Override
    public void processPage(@org.jetbrains.annotations.NotNull List<GetAllAddressesResponse> responses, long page) {
        System.out.println("Page: " + page);
        GetAllAddressesResponse address = getAddressResponse(urlAPI, page);
        responses.add(address);
        Address addressEntity = convertToAddressEntity(address);
        addressRepository.save(addressEntity);

        List<Model> models = addressEntity.getModels();
        for (Model model : models) {
            String url = model.getUrl();
            crunchifyWhois(url);
        }
        Utils.runLoggers(loggers, "urls");
    }

}
