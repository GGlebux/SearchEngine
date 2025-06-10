package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.models.Lemma;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
}
