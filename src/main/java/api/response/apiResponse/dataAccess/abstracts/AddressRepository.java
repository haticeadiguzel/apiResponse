package api.response.apiResponse.dataAccess.abstracts;

import api.response.apiResponse.entities.concretes.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
}
