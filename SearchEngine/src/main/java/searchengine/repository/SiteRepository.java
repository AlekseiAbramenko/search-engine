package searchengine.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends CrudRepository<SiteModel, Integer> {
    @Query("select s from SiteModel s")
    List<SiteModel> gelAllSites();
    @Query("select count(*) from SiteModel")
    int sitesCount();
    Optional<SiteModel> findSiteByUrl(String url);
}
