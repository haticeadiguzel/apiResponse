package api.response.apiResponse.entities.concretes;


import com.google.gson.annotations.SerializedName;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@RedisHash("whoises")
public class Whois implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @SerializedName("Domain Name")
    private String domainName;
    @SerializedName("Registry Domain ID")
    private String registryDomainID;
    @SerializedName("Registrar WHOIS Server")
    private String registrarWHOISServer;
    @SerializedName("Registrar URL")
    private String registrarURL;
    @SerializedName("Updated Date")
    private String updatedDate;
    @SerializedName("Creation Date")
    private String creationDate;
    @SerializedName("Registry Expiry Date")
    private String registryExpiryDate;
    @SerializedName("Registrar")
    private String registrar;
    @SerializedName("Registrar IANA ID")
    private String registrarIANAID;
    @SerializedName("Registrar Abuse Contact Email")
    private String registrarAbuseContactEmail;
    @SerializedName("Registrar Abuse Contact Phone")
    private String registrarAbuseContactPhone;
    @SerializedName("Domain Status")
    private String domainStatus;
    @SerializedName("Name Server")
    private String nameServer;
    @SerializedName("DNSSEC")
    private String DNSSEC;
    @SerializedName("URL of the ICANN Whois Inaccuracy Complaint Form")
    private String URLofTheICANNWhoisInaccuracyComplaintForm;
}