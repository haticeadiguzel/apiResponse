package api.response.apiResponse.business.concretes;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.AddressFetcherService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressFetcherManager implements AddressFetcherService {
    @Override
    public List<GetAllAddressesResponse> fetchAddressesData(String url) {
        return null;
    }
}
