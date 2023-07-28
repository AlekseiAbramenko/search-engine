package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends CrudRepository<SiteModel, Integer> {
    @Query("select s from SiteModel s")
    List<SiteModel> gelAllSites();
    @Query("select count(*) from SiteModel")
    int sitesCount();
    @Transactional
    @Modifying
    @Query("delete from SiteModel s where upper(s.url) = upper(?1)")
    void deleteSiteByUrl(String url);
    Optional<SiteModel> findSiteByUrl(String url);
}
