package api.response.apiResponse.business.concretes;

import api.response.apiResponse.Exceptions.*;
import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.ResponseConverterService;
import api.response.apiResponse.core.utilities.mappers.ModelMapperService;
import api.response.apiResponse.entities.concretes.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResponseConverterManager implements ResponseConverterService {
    final private ModelMapperService modelMapperService;

    public ResponseConverterManager(ModelMapperService modelMapperService) {
        this.modelMapperService = modelMapperService;
    }

    @Override
    public Address convertToAddressEntity(GetAllAddressesResponse addressResponse) {
        try {
            return this.modelMapperService.forResponse().map(addressResponse, Address.class);
        } catch (Exception e) {
            throw new ConvertToAddressEntityException("Error occurred while converting GetAllAddressesResponse to Address: ", e);
        }
    }
}
