package engine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import engine.models.Index;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
}
