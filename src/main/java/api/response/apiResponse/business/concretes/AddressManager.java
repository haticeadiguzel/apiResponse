package api.response.apiResponse.business.concretes;

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
import org.springframework.data.redis.core.RedisTemplate;
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
    @Value("${address.url}")
    private String url;
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;


    public AddressManager(AddressRepository addressRepository, ModelMapperService modelMapperService, RedisRepository redisRepository) {
        this.addressRepository = addressRepository;
        this.modelMapperService = modelMapperService;
        this.redisRepository = redisRepository;
    }

    @Scheduled(initialDelay = 1000, fixedRate = 21600000)
    @Override
    public List<GetAllAddressesResponse> getAddressesData() {
        List<GetAllAddressesResponse> responses = new ArrayList<>();
        Jedis jedis = new Jedis(host, port);
        Set<String> urls = new HashSet<>();
        int pageCount = 10;

//        WebClient.Builder builderPageCount = WebClient.builder();
//        GetAllAddressesResponse addressPageCount = builderPageCount.build()
//                .get()
//                .uri(url)
//                .retrieve()
//                .bodyToMono(GetAllAddressesResponse.class)
//                .block();
//        Address addressEntityPageCount = convertToAddressEntity(addressPageCount);
//        long pageCount = addressEntityPageCount.getPageCount();

        for (int page = 1; page < pageCount; page++) {
            try {
                WebClient.Builder builder = WebClient.builder();
                WebClient webClient = builder.baseUrl(url).build();
                String pageUrl = String.format("?page=%d", page);
                GetAllAddressesResponse address = webClient
                        .get()
                        .uri(pageUrl)
                        .retrieve()
                        .bodyToMono(GetAllAddressesResponse.class)
                        .block();
                responses.add(address);
                Address addressEntity = convertToAddressEntity(address);
                addressRepository.save(addressEntity);

                List<Model> models = addressEntity.getModels();
                for (Model model : models) {
                    String url = model.getUrl();
                    jedis.sadd("urls", url);
                    Whois whois = new Gson().fromJson(crunchifyWhois(url).toString(), Whois.class);
                    String DomainName = whois.getDomainName();
                    System.out.println("Domain Name: "+ DomainName);
                    redisRepository.save(whois);
                }
            } catch (WebClientResponseException.TooManyRequests e) {
                log.warn("Too many Get request to api...");
                try {
                    Thread.sleep(3000);
                    continue;
                } catch (Exception exception) {
                    log.error("Error: " + exception);
                }

            }
        }
        return responses;
    }

    @Override
    public Address convertToAddressEntity(GetAllAddressesResponse addressResponse) {
        return this.modelMapperService.forResponse().map(addressResponse, Address.class);
    }

    @Override
    public JSONObject crunchifyWhois(String url) {
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
            crunchifyWhois.disconnect();
            return jsonWhois;
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return null;
    }
}
