package api.response.apiResponse.dataAccess.abstracts;

import api.response.apiResponse.entities.concretes.Whois;
import org.springframework.data.repository.CrudRepository;

public interface RedisRepository extends CrudRepository<Whois, Long> {
}
