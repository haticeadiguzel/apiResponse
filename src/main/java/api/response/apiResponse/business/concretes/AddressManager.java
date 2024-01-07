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
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;
import com.google.gson.Gson;

import java.util.*;

@Service
@Slf4j
@EnableCaching
public class AddressManager implements AddressService {
    final private AddressRepository addressRepository;
    final private ModelMapperService modelMapperService;
    final private RedisRepository redisRepository;
    final private RedisTemplate<String, String> redisTemplate;
    @Value("${address.url}")
    private String url;

    public AddressManager(AddressRepository addressRepository, ModelMapperService modelMapperService, RedisRepository redisRepository, RedisTemplate<String, String> redisTemplate) {
        this.addressRepository = addressRepository;
        this.modelMapperService = modelMapperService;
        this.redisRepository = redisRepository;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(initialDelay = 1000, fixedRate = 21600000)
    @Override
    public List<GetAllAddressesResponse> getAddressesData() {
        List<GetAllAddressesResponse> responses = new ArrayList<>();
        Set<String> urls = new HashSet<>();
        long pageCount = 10;

//        WebClient.Builder builder0 = WebClient.builder();
//        GetAllAddressesResponse address0 = builder0.build()
//                .get()
//                .uri(url)
//                .retrieve()
//                .bodyToMono(GetAllAddressesResponse.class)
//                .block();
//        Address addressEntity0 = convertToAddressEntity(address0);
//        long pageCount = addressEntity0.getPageCount();


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
                    urls.add(url);
                }
            }
            catch (Exception exception) {
                log.error("Something went wrong while getting value from api: ", exception);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Exception while calling endpoint of api for urls."
                );
            }
        }
        System.out.println(urls);
//        saveUrls(urls);

        for (String url : urls) {
            if(crunchifyWhois(url).toString().equals("{}")){
                System.out.println("No match for " + url);
            } else {
                Whois whois = new Gson().fromJson(crunchifyWhois(url).toString(), Whois.class);
                String DomainName = whois.getDomainName();
                System.out.println("Domain Name: "+ DomainName);
//                redisRepository.save(whois);
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

    public void saveUrls(Set<String> urls) {
        redisTemplate.opsForSet().add("urls", urls.toArray(new String[0]));
    }
}