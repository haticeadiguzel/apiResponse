package api.response.apiResponse.business.concretes;

import api.response.apiResponse.Exceptions.SaveWhoisResultToRedisException;
import api.response.apiResponse.business.abstracts.RedisService;
import api.response.apiResponse.dataAccess.abstracts.RedisRepository;
import api.response.apiResponse.entities.concretes.Whois;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisManager implements RedisService {
    final private RedisRepository redisRepository;

    public RedisManager(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public void saveRedis(JSONObject jsonWhois) {
        try {
            Whois whois = new Gson().fromJson(jsonWhois.toString(), Whois.class);
            redisRepository.save(whois);
        } catch (Exception e) {
            log.error("Urls cannot saved to redis: ", e);
            throw new SaveWhoisResultToRedisException("Error occurred while connecting redis and saving data: ", e);
        }
    }
}
