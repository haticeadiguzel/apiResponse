package api.response.apiResponse.business.abstracts;

import api.response.apiResponse.entities.concretes.Address;

public interface DbService {
    void saveDB(Address addressEntity);
}
