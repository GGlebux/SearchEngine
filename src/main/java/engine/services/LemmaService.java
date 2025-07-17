package engine.services;

import engine.models.Lemma;
import engine.models.Site;
import engine.repositories.LemmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.IntFunction;

@Service
@Transactional(readOnly = true)
public class LemmaService {
    private final LemmaRepository repo;

    @Autowired
    public LemmaService(LemmaRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void saveAll(Collection<Lemma> lemmas) {
        Integer[] ids = getArray(lemmas, l -> l.getSite().getId(), Integer[]::new);
        String[] lems = getArray(lemmas, Lemma::getLemma, String[]::new);
        Long[] frequencies = getArray(lemmas, Lemma::getFrequency, Long[]::new);

        repo.bulkUpsertLemmas(ids, lems, frequencies);

    }

    @Transactional
    public void deleteAllBySite(Collection<Site> sites) {
        repo.deleteAllBySiteIn(sites);
    }

    private static <R> R[] getArray(Collection<Lemma> lemmas, Function<Lemma, R> mapper, IntFunction<R[]> array) {
        return lemmas
                .stream()
                .map(mapper)
                .toArray(array);
    }

}
