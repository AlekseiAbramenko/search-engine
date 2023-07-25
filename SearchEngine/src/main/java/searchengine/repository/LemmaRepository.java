package searchengine.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;

import java.util.Optional;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
    @Transactional
    @Modifying
    @Query("update Lemma l set l.frequency = ?1 where l.lemma = ?2")
    void updateLemmasFrequency(int frequency, String lemma);
    @Query("select l from Lemma l where l.lemma = ?1")
    Optional<Lemma> findLemmaByName(String lemma);
}
