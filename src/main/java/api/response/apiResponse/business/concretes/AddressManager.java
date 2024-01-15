package api.response.apiResponse.business.concretes;

import api.response.apiResponse.Exceptions.*;
import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.*;
import api.response.apiResponse.entities.concretes.Address;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import redis.clients.jedis.Jedis;

import java.util.*;

@Slf4j
@Service
@EnableCaching
public class AddressManager implements AddressService {
    final private WhoisRunnerService whoisRunnerService;
    final private PageProcessorService pageProcessorService;
    final private ResponseConverterService responseConverterService;
    @Value("${address.url}")
    private String urlAPI;
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    public AddressManager(WhoisRunnerService whoisRunnerService, PageProcessorService pageProcessorService, ResponseConverterService responseConverterService) {
        this.whoisRunnerService = whoisRunnerService;
        this.pageProcessorService = pageProcessorService;
        this.responseConverterService = responseConverterService;
    }

    @Scheduled(initialDelay = 1000, fixedRate = 21600000)
    @Override
    public void fetchAddressesData() {
        try {
            long totalCount = getTotalCountOfUrl(urlAPI);
            long pageCount = totalCount / 1550 + 1;
            log.info("Page Count: {}", pageCount);
            for (long page = 0; page < pageCount; page++) {
                pageProcessorService.processPage(page);
            }
        } catch (TooManyGetRequestException | ApiRequestException | WebClientResponseException e) {
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
    public long getTotalCountOfUrl(String url) {
        try {
            WebClient.Builder builderPageCount = WebClient.builder();
            GetAllAddressesResponse addressTotalCount = builderPageCount.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(GetAllAddressesResponse.class)
                    .block();
            Address addressEntityTotalCount = responseConverterService.convertToAddressEntity(addressTotalCount);
            return addressEntityTotalCount.getTotalCount();
        } catch (Exception e) {
            throw new GetTotalCountException("Error occurred while getting total count from api: ", e);
        }
    }

    @Override
    public List<Map<String, String>> getWhoisListResultFromRedis(List<String> urls) {
        List<Map<String, String>> results = new ArrayList<>();
        try (Jedis jedis = new Jedis(host, port);) {
            for (String url : urls) {
                whoisRunnerService.crunchifyWhois(url);
                Map<String, String> result = new HashMap<>(jedis.hgetAll("whoises:" + url));
                results.add(result);
            }
            return results;
        } catch (Exception e) {
            return null;
        }
    }
}