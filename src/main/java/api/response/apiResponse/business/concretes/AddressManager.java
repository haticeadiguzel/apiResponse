package api.response.apiResponse.business.concretes;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.AddressService;
import api.response.apiResponse.dataAccess.abstracts.AddressRepository;
import api.response.apiResponse.entities.concretes.Address;
import api.response.apiResponse.entities.concretes.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AddressManager implements AddressService {
    @Autowired
    private AddressRepository addressRepository;
    @Value("${address.url}")
    private String url;

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

    private Address convertToAddressEntity(GetAllAddressesResponse addressResponse) {
        // Bir Address entity oluştur ve özelliklerini ayarla
        Address addressEntity = new Address();
        addressEntity.setTotalCount(addressResponse.getTotalCount());
        addressEntity.setCount(addressResponse.getCount());
        addressEntity.setModels(convertToModelEntities(addressResponse.getModels()));
        addressEntity.setPage(addressResponse.getPage());
        addressEntity.setPageCount(addressResponse.getPageCount());

        return addressEntity;
    }

    private List<Model> convertToModelEntities(List<Model> models) {
        // Model DTO'ların bulunduğu listeyi Model entity'lerin bulunduğu liste olarak çevir
        return models.stream()
                .map(modelDto -> {
                    Model modelEntity = new Model();
                    modelEntity.setId(modelDto.getId());
                    modelEntity.setUrl(modelDto.getUrl());
                    modelEntity.setType(modelDto.getType());
                    modelEntity.setDesc(modelDto.getDesc());
                    modelEntity.setSource(modelDto.getSource());
                    modelEntity.setDate(modelDto.getDate());
                    modelEntity.setCriticality_level(modelDto.getCriticality_level());
                    modelEntity.setConnectiontype(modelDto.getConnectiontype());
                    return modelEntity;
                })
                .collect(Collectors.toList());
    }
}
