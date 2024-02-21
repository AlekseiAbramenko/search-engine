package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexRepository extends CrudRepository<IndexModel, Integer> {
    @Query("select i from IndexModel i where i.lemma = ?1 and i.page.site.url = ?2")
    List<IndexModel> findPagesByLemmaBySite(Lemma lemma, String url);

    @Query("select i from IndexModel i where i.page = ?1 and i.lemma = ?2")
    Optional<IndexModel> findIndex(Page page, Lemma lemma);

    @Query("select i from IndexModel i where i.lemma = ?1")
    List<IndexModel> findIndexesByLemma(Lemma lemma);

    @Query("select (count(i) > 0) from IndexModel i where i.page = ?1 and i.lemma = ?2")
    boolean existsIndex(Page page, Lemma lemma);

    @Query("select (count(i) > 0) from IndexModel i where i.page = ?1 and i.lemma.lemma = ?2")
    boolean existsIndex2(Page page, String lemmasName);

    @Transactional
    @Modifying
    @Query("delete from IndexModel i where i.page = ?1")
    void deleteIndexesByPage(Page page);

    @Transactional
    @Modifying
    @Query("delete from IndexModel i where i.lemma = ?1")
    void deleteIndexesByLemma(Lemma lemma);

    @Query("select i from IndexModel i where i.page = ?1")
    List<IndexModel> findIndexesByPage(Page page);

    @Transactional
    @Modifying
    @Query("update IndexModel i set i.rank = ?1 where i.page = ?2 and i.lemma = ?3")
    void updateIndex(float rank, Page page, Lemma lemma);
}
