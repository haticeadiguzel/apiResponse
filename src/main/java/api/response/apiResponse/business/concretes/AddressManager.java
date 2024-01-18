package api.response.apiResponse.business.concretes;

import api.response.apiResponse.Exceptions.*;
import api.response.apiResponse.business.DTOs.Responses.*;
import api.response.apiResponse.business.abstracts.*;
import api.response.apiResponse.entities.concretes.Address;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@EnableCaching
public class AddressManager implements AddressService {
    final private WhoisRunnerService whoisRunnerService;
    final private PageProcessorService pageProcessorService;
    final private ResponseConverterService responseConverterService;

    public AddressManager(WhoisRunnerService whoisRunnerService, PageProcessorService pageProcessorService, ResponseConverterService responseConverterService) {
        this.whoisRunnerService = whoisRunnerService;
        this.pageProcessorService = pageProcessorService;
        this.responseConverterService = responseConverterService;
    }

    private static final long INITIAL_DELAY = 1000;
    private static final long FIXED_RATE = 21600000;
    private static final String REDIS_KEY_PREFIX = "whoises:";

    @Value("${address.url}")
    private String urlAPI;
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    @Scheduled(initialDelay = INITIAL_DELAY, fixedRate = FIXED_RATE)
    @Override
    public void fetchAddressesData() {
        try {
            long totalCount = getTotalCountOfUrl(urlAPI);
            long pageCount = totalCount / 1550 + 1;
            log.info("Page Count: {}", pageCount);
            for (long page = 0; page < pageCount; page++) {
                pageProcessorService.processPage(page);
            }
        } catch (TooManyGetRequestException | ApiRequestException | WebClientResponseException e) {
            log.error("Error fetching addresses data", e);
            handleFetchAddressesDataException();
        }
    }

    private void handleFetchAddressesDataException() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException exception) {
            throw new TooManyGetRequestException("Too many get request from api...", exception);
        } catch (WebClientResponseException exception) {
            throw new ApiRequestException("Per-page value is bigger than 1550...", exception);
        }
    }

    @Override
    public long getTotalCountOfUrl(String url) {
        try {
            WebClient.Builder builderPageCount = WebClient.builder();
            GetAllAddressesResponse addressTotalCount = builderPageCount.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(GetAllAddressesResponse.class)
                    .block();
            Address addressEntityTotalCount = responseConverterService.convertToAddressEntity(addressTotalCount);
            return addressEntityTotalCount.getTotalCount();
        } catch (Exception exception) {
            throw new GetTotalCountException("Error occurred while getting total count from api: ", exception);
        }
    }

    @Override
    public List<WhoisResultResponse> getWhoisListResultFromRedis(@NotNull List<String> urls) {
        List<WhoisResultResponse> results = new ArrayList<>();
        ZonedDateTime nowDate = ZonedDateTime.now(ZoneId.of("Z"));
        try (Jedis jedis = new Jedis(host, port)) {
            for (String url : urls) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                whoisRunnerService.crunchifyWhois(url);
                Map<String, String> result = jedis.hgetAll(REDIS_KEY_PREFIX + url);
                String domainStatus = result.get("domainStatus");
                String registryExpiryDate = result.get("registryExpiryDate");

                if (registryExpiryDate != null || domainStatus != null) {
                    ZonedDateTime RegistryExpiryDate = ZonedDateTime.parse(registryExpiryDate);

                    if (nowDate.isBefore(RegistryExpiryDate) || domainStatus.contains("ok") || domainStatus.contains("addPeriod") || domainStatus.contains("autoRenewPeriod") || domainStatus.contains("renewPeriod")) {
                        result.put("status", "Active");
                    } else if (nowDate.isAfter(RegistryExpiryDate) || domainStatus.contains("inactive")) {
                        result.put("status", "Inactive");
                    } else {
                        result.put("status", "Last day of Registry Expiry Date");
                    }
                } else {
                    result.put("status", "There is no Whois information...");
                }
                results.add(mapper.convertValue(result, WhoisResultResponse.class));
            }
            return results;
        } catch (JedisConnectionException exception) {
            throw new RedisConnectionException("Error occurred while connecting redis: ", exception);
        }
    }
}

 /*
Aktif bir alan adını gösteren durum kodları şunlardır:
ok: Bu, alan adının etkin ve kullanılabilir olduğu anlamına gelen en yaygın koddur. Herhangi bir bekleyen işlem veya kısıtlama yoktur.
addPeriod: Bu, alan adının eklenme sürecinde olduğunu, ancak henüz tamamen etkin olmadığını gösterir.
autoRenewPeriod: Alan adı otomatik olarak yenilenmektedir ve kayıt süresi dolmak üzeredir, ancak yine de etkindir.
renewPeriod: Alan adı yenileme sürecindedir, ancak yine de etkindir.

Diğer durum kodları, alan adının etkin olmadığını veya bazı kısıtlamalara tabi olduğunu gösterir:
inactive: Alan adı etkin değildir ve kullanılamaz.
pendingCreate: Alan adı oluşturulma sürecindedir.
pendingDelete: Alan adı silinme sürecindedir.
pendingRenew: Alan adı yenileme sürecindedir.
pendingRestore: Alan adı daha önce silinmiş ve geri yükleme sürecindedir.
pendingTransfer: Alan adı başka bir kayıt şirketine aktarılma sürecindedir.
pendingUpdate: Alan adında değişiklikler yapılmaktadır.
redemptionPeriod: Alan adı süresi dolmuş ve kurtarma dönemindedir.
transferPeriod: Alan adı başka bir kayıt şirketine aktarılma sürecindedir.
serverDeleteProhibited, serverHold, serverRenewProhibited, serverTransferProhibited, serverUpdateProhibited: Bu kodlar, kayıt şirketi tarafından alan adında kısıtlamalar olduğunu gösterir.
clientDeleteProhibited, clientHold, clientRenewProhibited, clientTransferProhibited, clientUpdateProhibited: Bu kodlar, alan adı sahibi tarafından alan adında kısıtlamalar olduğunu gösterir.
*/