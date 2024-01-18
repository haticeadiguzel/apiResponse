package api.response.apiResponse.business.concretes;

import api.response.apiResponse.Exceptions.*;
import api.response.apiResponse.business.abstracts.DbService;
import api.response.apiResponse.dataAccess.abstracts.AddressRepository;
import api.response.apiResponse.entities.concretes.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DbManager implements DbService {
    final private AddressRepository addressRepository;

    public DbManager(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public void saveDB(Address addressEntity) {
        try {
            addressRepository.save(addressEntity);
            log.info("Data saved to DB successfully.");
        } catch (DataAccessException exception) {
            log.error("Error occurred while saving data to DB: {}", exception.getMessage());
            throw new SaveToDbException("Error occurred while saving data to DB", exception);
        }
    }
}
