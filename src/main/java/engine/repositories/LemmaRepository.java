package engine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import engine.models.Lemma;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
}
