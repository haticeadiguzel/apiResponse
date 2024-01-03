package api.response.apiResponse.business.concretes;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.AddressService;
import api.response.apiResponse.core.utilities.mappers.ModelMapperService;
import api.response.apiResponse.dataAccess.abstracts.AddressRepository;
import api.response.apiResponse.entities.concretes.Address;
import api.response.apiResponse.entities.concretes.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AddressManager implements AddressService {

    final private AddressRepository addressRepository;
    final private ModelMapperService modelMapperService;
    @Value("${address.url}")
    private String url;

    public AddressManager(AddressRepository addressRepository, ModelMapperService modelMapperService) {
        this.addressRepository = addressRepository;
        this.modelMapperService = modelMapperService;
    }

    @Scheduled(initialDelay = 1000, fixedRate = 21600000)
    @Override
    public List<GetAllAddressesResponse> getAddressesData() {
        List<GetAllAddressesResponse> responses = new ArrayList<>();
        ArrayList<String> urls = new ArrayList<>();
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
            } catch (Exception exception) {
                log.error("Something went wrong while getting value from api: ", exception);
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Exception while calling endpoint of api for urls."
                );
            }
        }
        System.out.println("Urls: " + urls.size());
        return responses;
    }

    @Override
    public Address convertToAddressEntity(GetAllAddressesResponse addressResponse) {
        return this.modelMapperService.forResponse().map(addressResponse, Address.class);
    }
}
