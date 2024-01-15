package api.response.apiResponse.business.concretes;

import api.response.apiResponse.Exceptions.*;
import api.response.apiResponse.Logger.Logger;
import api.response.apiResponse.Logger.Utils;
import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.AddressFetcherService;
import api.response.apiResponse.business.abstracts.AddressService;
import api.response.apiResponse.business.abstracts.WhoisProcessorService;
import api.response.apiResponse.core.utilities.mappers.ModelMapperService;
import api.response.apiResponse.dataAccess.abstracts.AddressRepository;
import api.response.apiResponse.dataAccess.abstracts.RedisRepository;
import api.response.apiResponse.entities.concretes.Address;
import api.response.apiResponse.entities.concretes.Model;
import api.response.apiResponse.entities.concretes.Whois;
import org.apache.commons.net.whois.WhoisClient;
import org.jetbrains.annotations.NotNull;
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

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@EnableCaching
public class AddressManager implements AddressService {
    final private AddressRepository addressRepository;
    final private ModelMapperService modelMapperService;
    final private RedisRepository redisRepository;
    final private AddressFetcherService addressFetcherService;
    final private WhoisProcessorService whoisProcessorService;
    final private Logger[] loggers;
    @Value("${address.url}")
    private String urlAPI;
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    public AddressManager(AddressRepository addressRepository, ModelMapperService modelMapperService, RedisRepository redisRepository, AddressFetcherService addressFetcherService, WhoisProcessorService whoisProcessorService, Logger[] loggers) {
        this.addressRepository = addressRepository;
        this.modelMapperService = modelMapperService;
        this.redisRepository = redisRepository;
        this.addressFetcherService = addressFetcherService;
        this.whoisProcessorService = whoisProcessorService;
        this.loggers = loggers;
    }

    @Scheduled(initialDelay = 1000, fixedRate = 21600000)
    @Override
    public void fetchAddressesData() {
        try {
            long totalCount = getTotalCountOfUrl(urlAPI);
            long pageCount = totalCount / 1550 + 1;
            System.out.println(pageCount);
            for (long page = 0; page < pageCount; page++) {
                processPage(page);
            }
        } catch (Exception e) {
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
    public Map<String, String> getWhoisResultFromRedis(String url) {
        try (Jedis jedis = new Jedis(host, port)) {
            return jedis.hgetAll("whoises:" + url);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Map<String, String>> getWhoisListResultFromRedis(List<String> urls) {
        List<Map<String, String>> results = new ArrayList<>();
        try (Jedis jedis = new Jedis(host, port);) {
            for (String url : urls) {
                Map<String, String> result = new HashMap<>(jedis.hgetAll("whoises:" + url));
                results.add(result);
            }
            return results;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Address convertToAddressEntity(GetAllAddressesResponse addressResponse) {
        return this.modelMapperService.forResponse().map(addressResponse, Address.class);
    }

    @Override
    public void crunchifyWhois(String url) {
        String whoisResultString = getWhoisResultFromWhoisApi(url);
        JSONObject jsonWhois = parseWhoisResult(whoisResultString, url);
        saveRedis(jsonWhois);
        System.out.println(url);
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

    @Override
    public void saveRedis(@NotNull JSONObject jsonWhois) {
        try {
            Whois whois = new Gson().fromJson(jsonWhois.toString(), Whois.class);
            redisRepository.save(whois);
        } catch (Exception e) {
            throw new SaveWhoisResultToRedisException("Error occurred while connecting redis and saving data: ", e);
        }
    }

    @Override
    public long getTotalCountOfUrl(String url) {
        try {
            WebClient.Builder builderPageCount = WebClient.builder();
            GetAllAddressesResponse addressTotalCount = builderPageCount.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(GetAllAddressesResponse.class)
                    .block();
            Address addressEntityTotalCount = convertToAddressEntity(addressTotalCount);
            return addressEntityTotalCount.getTotalCount();
        } catch (Exception e) {
            throw new GetTotalCountException("Error occurred while getting total count from api: ", e);
        }
    }

    @Override
    public GetAllAddressesResponse getAddressResponse(String url, long page) {
        try {
            WebClient.Builder builder = WebClient.builder();
            WebClient webClient = builder.baseUrl(url).build();
            String pageUrl = String.format("?page=%d&per-page=1550", page);
            return webClient
                    .get()
                    .uri(pageUrl)
                    .retrieve()
                    .bodyToMono(GetAllAddressesResponse.class)
                    .block();
        } catch (Exception e) {
            throw new GetAllAddressesResponseException("Error occurred while getting all addresses from api: ", e);
        }
    }

    @Override
    public void processPage(long page) {
        System.out.println("Page: " + page);
        GetAllAddressesResponse address = getAddressResponse(urlAPI, page);
        Address addressEntity = convertToAddressEntity(address);
        saveDB(addressEntity);
        runCrunchifyWhoisToAllUrl(addressEntity);
        Utils.runLoggers(loggers, "urls");
    }

    @Scheduled(initialDelay = 500, fixedRate = 21600000)
    @Override
    public List<Map<String, String>> defaultListUrl() {
        try {
            List<String> urls = new ArrayList<>();
            urls.add("google.com");
            for (String url : urls) {
                crunchifyWhois(url);
            }
            return getWhoisListResultFromRedis(urls);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void saveDB(Address addressEntity) {
        try {
            addressRepository.save(addressEntity);
        } catch (Exception e) {
            log.info("Error save data.");
        }
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
            log.info("Error run crunchify whois to all urls.");
        }

    }
}