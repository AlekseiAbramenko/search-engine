package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
    @Query("select count(l) from Lemma l where l.site = ?1")
    int lemmasCountBySite(SiteModel site);
    @Transactional
    @Modifying
    @Query("update Lemma l set l.frequency = ?1 where l = ?2")
    void updateLemmasFrequency(int newFrequency, Lemma lemma);
    @Query("select l from Lemma l where l.lemma = ?1")
    List<Lemma> findLemmasListByName(String lemma);
    @Query("select l from Lemma l where l.lemma = ?1 and l.site = ?2")
    List<Lemma> findLemmasList(String lemma, SiteModel site);
    @Query("select l from Lemma l where l.lemma = ?1 and l.site = ?2")
    Optional<Lemma> findLemma(String lemma, SiteModel site);
    @Transactional
    @Modifying
    @Query("delete from Lemma l where l.site = ?1")
    void deleteLemmasBySite(SiteModel site);

}
