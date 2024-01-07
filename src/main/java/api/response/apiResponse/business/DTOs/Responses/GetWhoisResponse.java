package api.response.apiResponse.business.DTOs.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetWhoisResponse {
    private Long id;
    private String domainName;
    private String registryDomainID;
    private String registrarWHOISServer;
    private String registrarURL;
    private String updatedDate;
    private String creationDate;
    private String registryExpiryDate;
    private String registrar;
    private String registrarIANAID;
    private String registrarAbuseContactEmail;
    private String registrarAbuseContactPhone;
    private String domainStatus;
    private String nameServer;
    private String DNSSEC;
    private String URLofTheICANNWhoisInaccuracyComplaintForm;
}
