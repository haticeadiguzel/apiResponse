package api.response.apiResponse.business.abstracts;

import org.json.JSONObject;

public interface RedisService{
    void saveRedis(JSONObject jsonWhois);
}
