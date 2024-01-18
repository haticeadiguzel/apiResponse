package api.response.apiResponse.business.DTOs.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhoisResultResponse {
    private String id;
    private String registryExpiryDate;
    private String domainStatus;
    private String status;
}
