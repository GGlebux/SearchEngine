package engine.repositories;

import engine.models.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import engine.models.Lemma;

import java.util.Collection;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    @Modifying
    @Query(value = """
    INSERT INTO lemma (site_id, lemma, frequency)
    SELECT * FROM UNNEST(?1::bigint[], ?2::text[], ?3::bigint[])
    ON CONFLICT ON CONSTRAINT uk_lemma_site
    DO UPDATE SET frequency = lemma.frequency + EXCLUDED.frequency RETURNING *""",
            nativeQuery = true)
    void bulkUpsertLemmas(
            @Param("siteIds") Integer[] siteIds,
            @Param("lemmas") String[] lemmas,
            @Param("frequencies") Long[] frequencies
    );

    @Modifying
    @Query("DELETE FROM Lemma l WHERE l.site IN :sites")
    void deleteAllBySiteIn(@Param("sites") Collection<Site> sites);
}
