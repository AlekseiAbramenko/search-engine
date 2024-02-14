package searchengine.workers;

import searchengine.dto.indexing.LocalDB;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SiteModel;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class LemmaAndIndexCollector extends RecursiveTask<LocalDB> {
    private SiteModel siteModel;
    private List<Page> pagesList;
    private CopyOnWriteArraySet<IndexModel> indexesSet;
    private ConcurrentHashMap<String, Lemma> lemmasMap;

    public LemmaAndIndexCollector(SiteModel siteModel, List<Page> pagesList, CopyOnWriteArraySet<IndexModel> indexesSet, ConcurrentHashMap<String, Lemma> lemmasMap) {
        this.siteModel = siteModel;
        this.pagesList = pagesList;
        this.indexesSet = indexesSet;
        this.lemmasMap = lemmasMap;
    }

    @Override
    protected LocalDB compute() {
        if (pagesList.size() > 1) {
            createSubtask().forEach(ForkJoinTask::join);
        } else {
            worker(pagesList.getFirst());
        }
        return new LocalDB(indexesSet, List.copyOf(lemmasMap.values()));
    }

    private Collection<LemmaAndIndexCollector> createSubtask() {
        List<LemmaAndIndexCollector> taskList = new ArrayList<>();
        pagesList.forEach(page -> {
            List<Page> onePage = new ArrayList<>();
            onePage.add(page);
            LemmaAndIndexCollector task = new LemmaAndIndexCollector(siteModel, onePage, indexesSet, lemmasMap);
            task.fork();
            taskList.add(task);
        });
        return taskList;
    }

    private synchronized void worker(Page page) {
        LemmaParser lemmaParser = new LemmaParser();
        HashMap<String, Integer> pageLemmas;
        try {
            pageLemmas = lemmaParser.countLemmasFromText(page.getContent());
            pageLemmas.forEach((lemmasName, rank) -> {
                Lemma lemma;
                if (lemmasMap.containsKey(lemmasName)) {
                    lemma = lemmasMap.get(lemmasName);
                    lemma.setFrequency(lemma.getFrequency() + 1);
                } else {
                    lemma = newLemma(lemmasName);
                }
                lemmasMap.put(lemmasName, lemma);
                indexesSet.add(newIndexModel(lemma, page, rank));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private IndexModel newIndexModel(Lemma lemma, Page page, Integer rank) {
        IndexModel indexModel = new IndexModel();
        indexModel.setLemma(lemma);
        indexModel.setPage(page);
        indexModel.setRank(rank);
        return indexModel;
    }

    private Lemma newLemma(String lemmasName) {
        Lemma lemma = new Lemma();
        lemma.setFrequency(1);
        lemma.setLemma(lemmasName);
        lemma.setSite(siteModel);
        return lemma;
    }
}
