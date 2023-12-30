package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.business.DTOs.Responses.GetAllUrls;
import api.response.apiResponse.entities.concretes.Model;

import java.util.List;

public interface ModelService {
    List<GetAllUrls> getAllUrls();
}
