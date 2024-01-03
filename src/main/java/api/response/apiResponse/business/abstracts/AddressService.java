package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.entities.concretes.Address;

import java.util.List;

public interface AddressService {
    List<GetAllAddressesResponse> getAddressesData();
    Address convertToAddressEntity(GetAllAddressesResponse addressResponse);
}
