package api.response.apiResponse.dataAccess.abstracts;

import api.response.apiResponse.entities.concretes.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, Integer>
{
}
