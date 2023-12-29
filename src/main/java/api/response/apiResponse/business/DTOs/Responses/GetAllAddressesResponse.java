package api.response.apiResponse.business.DTOs.Responses;

import api.response.apiResponse.entities.concretes.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllAddressesResponse {
    private int id;
    private long totalCount;
    private int count;
    List<Model> models;
    private int page;
    private long pageCount;
}
