package api.response.apiResponse.webApi.controllers;

import api.response.apiResponse.business.DTOs.Responses.GetAllUrls;
import api.response.apiResponse.business.abstracts.ModelService;
import api.response.apiResponse.entities.concretes.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ModelsController {
    @Autowired
    private ModelService modelService;

    @GetMapping("/geturls")
    public List<GetAllUrls> getAllUrlsFromDbs() {
        return modelService.getAllUrls();
    }
}
