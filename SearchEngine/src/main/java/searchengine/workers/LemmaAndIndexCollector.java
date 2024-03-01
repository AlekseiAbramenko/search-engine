package searchengine.workers;

import searchengine.dto.indexing.LocalDB;
import searchengine.model.*;

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

    public LemmaAndIndexCollector(CollectorParameters collectorParameters) {
        this.siteModel = collectorParameters.getSiteModel();
        this.pagesList = collectorParameters.getPagesList();
        this.indexesSet = collectorParameters.getIndexesSet();
        this.lemmasMap = collectorParameters.getLemmasMap();
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
            CollectorParameters collectorParameters = new CollectorParameters(siteModel, onePage, indexesSet, lemmasMap);
            LemmaAndIndexCollector task = new LemmaAndIndexCollector(collectorParameters);
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
                    lemma = new Lemma(siteModel, lemmasName, 1);
                }
                lemmasMap.put(lemmasName, lemma);
                indexesSet.add(new IndexModel(page, lemma, rank));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}