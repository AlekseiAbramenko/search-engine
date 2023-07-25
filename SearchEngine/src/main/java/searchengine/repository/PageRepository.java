package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
    @Query("select p from Page p where p.site = ?1")
    List<Page> findPagesBySite(SiteModel site);
    @Query("select p from Page p where p.path = ?1")
    Optional<Page> findPage(String path);
    @Transactional
    @Modifying
    @Query("delete from Page p where upper(p.path) = upper(?1)")
    void deletePageByUrl(String path);
    @Query("select p from Page p where upper(p.path) = upper(?1)")
    Optional<Page> findSiteByPage(String path);
    @Query("select (count(p) > 0) from Page p where p.path = ?1")
    boolean existPage(String path);
    @Transactional
    @Modifying
    @Query("delete from Page p where p.site = ?1")
    void deletePagesBySiteId(SiteModel site);
}
