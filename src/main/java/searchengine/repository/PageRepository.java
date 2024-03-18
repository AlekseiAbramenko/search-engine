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
    @Query("select count(p) from Page p where p.site = ?1")
    int pagesCountBySite(SiteModel site);

    @Query("select p from Page p where p.site = ?1")
    List<Page> findPagesBySite(SiteModel site);

    @Query("select p from Page p where p.path = ?1 and p.site = ?2")
    Optional<Page> findPage(String path, SiteModel site);

    @Transactional
    @Modifying
    @Query("delete from Page p where p.site = ?1")
    void deletePagesBySite(SiteModel site);
}
