package api.response.apiResponse.business.concretes;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AddressManager{
    private static final String url = "https://www.usom.gov.tr/api/address/index";
    public Object getAddressData() {

		WebClient.Builder builder = WebClient.builder();

        GetAllAddressesResponse address = builder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(GetAllAddressesResponse.class)
                .block();

        return address;
    }
}
