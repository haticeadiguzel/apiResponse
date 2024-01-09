package api.response.apiResponse.webApi.controllers;

import api.response.apiResponse.business.abstracts.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AddressesController {
    @Autowired
    private AddressService addressService;

    @GetMapping("/address/index")
    public void getAll(){
        addressService.getAddressesData();
    }

    @GetMapping("/getWhois/{url}")
    public String getWhois(@PathVariable String url){
        return addressService.getWhois(url);
    }

//    @GetMapping("/address/list")
//    public String getWhoisList()
}
