package api.response.apiResponse.business.concretes;

import api.response.apiResponse.Exceptions.*;
import api.response.apiResponse.business.DTOs.Responses.GetAllAddressesResponse;
import api.response.apiResponse.business.DTOs.Responses.WhoisResultResponse;
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

import java.time.LocalDateTime;
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
    @Value("${address.url}")
    private String urlAPI;
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

    public AddressManager(WhoisRunnerService whoisRunnerService, PageProcessorService pageProcessorService, ResponseConverterService responseConverterService) {
        this.whoisRunnerService = whoisRunnerService;
        this.pageProcessorService = pageProcessorService;
        this.responseConverterService = responseConverterService;
    }

//    @Scheduled(initialDelay = 1000, fixedRate = 21600000)
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
            try {
                Thread.sleep(3000);
            } catch (InterruptedException exception) {
                throw new TooManyGetRequestException("Too many get request from api...", exception);
            } catch (WebClientResponseException exception) {
                throw new ApiRequestException("Per-page value is bigger than 1550...", exception);
            }
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
                Map<String, String> result = new HashMap<>(jedis.hgetAll("whoises:" + url));
                if (result.get("registryExpiryDate") != null) {
                    ZonedDateTime registryExpiryDate = ZonedDateTime.parse(result.get("registryExpiryDate"));
                    if (nowDate.isBefore(registryExpiryDate)) {
                        result.put("status", "Active");
                    } else if (nowDate.isAfter(registryExpiryDate)) {
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
addPeriod,autoRenewPeriod,inactive,ok,pendingCreate,pendingDelete,pendingRenew,
pendingRestore,pendingTransfer,pendingUpdate,redemptionPeriod,renewPeriod,
serverDeleteProhibited,serverHold,serverRenewProhibited,serverTransferProhibited,
serverUpdateProhibited,transferPeriod,clientDeleteProhibited,
clientHold,clientRenewProhibited,clientTransferProhibited,clientUpdateProhibited

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


ACTIVE – Bir alan adının normal durumu. Aktif alan adları web siteleri, e-postalar veya ad sunucularını kaydetmek için kullanılabilir.
OK – Alan adı aktif ve kullanılabilir.
INACTIVE – Alan adı, ad sunucuları ayarlanmamış veya yanlış olduğundan kullanılamıyor.
REGISTRAR-HOLD – Kayıt şirketi alanı beklemeye aldı ve kullanılamaz.
REGISTRY-HOLD – Merkezi kayıt defteri, alanı beklemeye aldı ve kullanılamaz.
REGISTRAR-LOCK – Alan adı kayıt şirketi tarafından kilitlenir. Alan adı değiştirilemez veya silinemez. Domain kullanılabilir ve yenilenebilir. Bu genellikle alan adı tescil ettireninin talebi üzerine yapılır.
REGISTRY-LOCK – Merkezi kayıt defteri, ayarları değiştirilemeyecek şekilde etki alanını kilitledi.
REDEMPTIONPERIOD – Alan adının süresi dolmuştur ve 30 günlük Geri Alma Süresi altındadır. Alan adı kullanılamaz, değiştirilemez ve silinemez. Alan adı yalnızca geri alma ücretleri ödenerek geri yüklenebilir.
PENDINGRESTORE – Süresi dolmuş bir alan adı şimdi AKTİF durumuna geri yükleniyor.
PENDINGDELETE – Etki alanının süresi dolmuş, kayıt defteri onu silmek üzere. Bir alan adı silinmeden önce beş gün boyunca bu durumda kalır.
CLIENT_DELETE_PROHIBITED – Alan adı, sponsorluk yapan kayıt şirketi tarafından silinemeyecek şekilde kilitlenir.
SERVER_DELETE_PROHIBITED – Alan adı kayıt defteri tarafından silinemeyecek şekilde kilitlenir.
CLIENT_HOLD – Alan adı, sponsor kayıt şirketi tarafından kullanılamayacak şekilde kilitlenir.
SERVER_HOLD – Alan adı kayıt defteri tarafından kullanılamayacak şekilde kilitlenir.
CLIENT_RENEW_PROHIBITED – Alan adı, yenilenemeyecek şekilde sponsor kayıt şirketi tarafından kilitlenir.
SERVER_RENEW_PROHIBITED – Alan adı kayıt defteri tarafından yenilenemeyecek şekilde kilitlenir.
CLIENT_TRANSFER_PROHIBITED – Alan adı, başka bir kayıt şirketine aktarılamayacak şekilde sponsor kayıt şirketi tarafından kilitlenir.
SERVER_TRANSFER_PROHIBITED – Alan adı, başka bir kayıt kuruluşuna aktarılamayacak şekilde kayıt defteri tarafından kilitlenir.
CLIENT_UPDATE_PROHIBITED – Alan adı sponsor kayıt şirketi tarafından kilitlendiğinden ayarları değiştirilemez.
SERVER_UPDATE_PROHIBITED – Merkezi kayıt defteri, ayarlarının değiştirilmesini önlemek için etki alanını kilitledi.
PENDING_DELETE – Alan adı kayıt defteri tarafından silinme sürecinde.
PENDING_TRANSFER – Alan adı bir kayıt şirketinden diğerine aktarılma sürecindedir. Bu süre içerisinde alan adında değişiklik yapılamaz.

        List<String> whoisStatuses = Arrays.asList(
                "addPeriod", "autoRenewPeriod", "inactive", "ok", "pendingCreate", "pendingDelete", "pendingRenew",
                "pendingRestore", "pendingTransfer", "pendingUpdate", "redemptionPeriod", "renewPeriod",
                "serverDeleteProhibited", "serverHold", "serverRenewProhibited", "serverTransferProhibited",
                "serverUpdateProhibited", "transferPeriod", "clientDeleteProhibited",
                "clientHold", "clientRenewProhibited", "clientTransferProhibited", "clientUpdateProhibited");
        for (String whoisStatus : whoisStatuses) {
            System.out.println(whoisStatus);
        }

*/