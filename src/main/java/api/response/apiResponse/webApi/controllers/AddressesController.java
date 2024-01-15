package api.response.apiResponse.webApi.controllers;

import api.response.apiResponse.business.abstracts.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AddressesController {
    @Autowired
    private AddressService addressService;

    @GetMapping("/address/index")
    public void getAll() {
        addressService.fetchAddressesData();
    }

    @GetMapping("/getWhois/{url}")
    public Map<String, String> getWhois(@PathVariable String url) {
        return addressService.getWhoisResultFromRedis(url);
    }

    @GetMapping("/getWhois/{urls}")
    public List<Map<String, String>> getWhoisList(@PathVariable List<String> urls) {
        return addressService.getWhoisListResultFromRedis(urls);
    }

    @GetMapping("/getWhoisFromDefaultList")
    public List<Map<String, String>>  getWhoisListScheduled() {
        return addressService.defaultListUrl();
    }
}
