package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends CrudRepository<SiteModel, Integer> {
    @Query("select s from SiteModel s")
    List<SiteModel> getAllSites();

    @Query("select count(*) from SiteModel")
    int sitesCount();

    Optional<SiteModel> findSiteByUrl(String url);

    @Transactional
    @Modifying
    @Query("update SiteModel s set s.statusTime = ?1 where s = ?2")
    void updateStatusTime(LocalDateTime statusTime, SiteModel siteModel);
}
