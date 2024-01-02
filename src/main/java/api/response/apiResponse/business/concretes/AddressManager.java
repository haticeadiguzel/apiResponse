package api.response.apiResponse.business.concretes;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.AddressService;
import api.response.apiResponse.core.utilities.mappers.ModelMapperService;
import api.response.apiResponse.dataAccess.abstracts.AddressRepository;
import api.response.apiResponse.entities.concretes.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class AddressManager implements AddressService {
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private ModelMapperService modelMapperService;
    @Value("${address.url}")
    private String url;

    @Scheduled(initialDelay = 1000, fixedRate =  1000 * 60 * 6)
    @Override
    public GetAllAddressesResponse getAddressesData() {
        try {
            WebClient.Builder builder = WebClient.builder();
            GetAllAddressesResponse address = builder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(GetAllAddressesResponse.class)
                    .block();

            Address addressEntity = convertToAddressEntity(address);
            addressRepository.save(addressEntity);
            log.info("Output from api: {}", address);

            return address;
        } catch (Exception exception) {
            log.error("Something went wrong while getting value from api: ", exception);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Exception while calling endpoint of api for urls."
            );
        }
    }

    @Override
    public Address convertToAddressEntity(GetAllAddressesResponse addressResponse) {
        Address address = this.modelMapperService.forResponse().map(addressResponse, Address.class);
        log.info("Address added to DB");
        return address;
    }
}