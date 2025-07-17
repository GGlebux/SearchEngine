package engine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import engine.models.Page;
import engine.models.Site;

import java.util.Collection;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    @Modifying
    @Query("DELETE FROM Page p WHERE p.site IN :sites")
    void deleteAllBySiteIn(@Param("sites") Collection<Site> sites);

    @Modifying
    @Query(value = """
            INSERT INTO page (site_id, path, code, content)
            VALUES (:siteId, :path, :code, :content)
            ON CONFLICT ON CONSTRAINT uk_page_site_path DO NOTHING""",
            nativeQuery = true)
    void insertOnConflict(@Param("siteId") int siteId,
                          @Param("path") String path,
                          @Param("code") int code,
                          @Param("content") String content);
}
