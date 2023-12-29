package api.response.apiResponse.webApi.controllers;

import api.response.apiResponse.business.concretes.AddressManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AddressesController {
    private final AddressManager addressManager;
    @GetMapping("/address/index")
    public ResponseEntity<?> callEndpointToGetData(){
        return ResponseEntity.ok(addressManager.getAddressData());
    }
}
