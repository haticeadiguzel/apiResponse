package api.response.apiResponse.business.concretes;

import api.response.apiResponse.Exceptions.GetAllAddressesResponseException;
import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.DbService;
import api.response.apiResponse.business.abstracts.PageProcessorService;
import api.response.apiResponse.business.abstracts.ResponseConverterService;
import api.response.apiResponse.business.abstracts.WhoisRunnerService;
import api.response.apiResponse.entities.concretes.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class PageProcessorManager implements PageProcessorService {
    final private DbService dbService;
    final private WhoisRunnerService whoisRunnerService;
    final private ResponseConverterService responseConverterService;

    @Value("${address.url}")
    private String urlAPI;

    public PageProcessorManager(DbService dbService, WhoisRunnerService whoisRunnerService, ResponseConverterService responseConverterService) {
        this.dbService = dbService;
        this.whoisRunnerService = whoisRunnerService;
        this.responseConverterService = responseConverterService;
    }

    @Override
    public void processPage(long page) {
        log.info("Page: " + page);
        GetAllAddressesResponse address = getAddressResponse(urlAPI, page);
        Address addressEntity = responseConverterService.convertToAddressEntity(address);
        dbService.saveDB(addressEntity);
        whoisRunnerService.runCrunchifyWhoisToAllUrl(addressEntity);
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
}
