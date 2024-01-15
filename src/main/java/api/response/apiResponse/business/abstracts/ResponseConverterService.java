package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.entities.concretes.Address;

public interface ResponseConverterService {
    Address convertToAddressEntity(GetAllAddressesResponse addressResponse);
}
