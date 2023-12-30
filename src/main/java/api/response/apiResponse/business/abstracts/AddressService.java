package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.entities.concretes.Address;

public interface AddressService {
    GetAllAddressesResponse getAddressesData();
    Address convertToAddressEntity(GetAllAddressesResponse addressResponse);
}
