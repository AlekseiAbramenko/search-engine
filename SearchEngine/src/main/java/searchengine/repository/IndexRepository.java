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

@Repository
public interface IndexRepository extends CrudRepository<IndexModel, Integer> {
    @Transactional
    @Modifying
    @Query("delete from IndexModel i where i.lemma = ?1")
    int deleteIndexByLemma(Lemma lemma);
    @Query("select (count(i) > 0) from IndexModel i where i.page = ?1 and i.lemma = ?2")
    boolean existsIndex(Page page, Lemma lemma);
    @Transactional
    @Modifying
    @Query("delete from IndexModel i where i.page = ?1")
    void deleteIndexByPage(Page page);
    @Query("select i from IndexModel i where i.page = ?1")
    List<IndexModel> findLemmasByPage(Page page);
    @Transactional
    @Modifying
    @Query("update IndexModel i set i.rank = ?1 where i.page = ?2 and i.lemma = ?3")
    void updateIndex(float rank, Page page, Lemma lemma);
}
