package api.response.apiResponse.webApi.controllers;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AddressesController {
    private final AddressService addressService;

    @GetMapping("/address/index")
    public GetAllAddressesResponse getAll(){
        return addressService.getAddressesData();
    }
}
