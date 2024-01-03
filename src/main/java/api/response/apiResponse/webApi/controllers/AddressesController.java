package api.response.apiResponse.webApi.controllers;

import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.abstracts.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AddressesController {
    @Autowired
    private AddressService addressService;

    @GetMapping("/address/index")
    public List<GetAllAddressesResponse> getAll(){
        return addressService.getAddressesData();
    }
}
