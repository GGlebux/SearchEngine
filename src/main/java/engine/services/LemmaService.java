package engine.services;

import engine.models.Lemma;
import engine.repositories.LemmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LemmaService {
    private final LemmaRepository repo;

    @Autowired
    public LemmaService(LemmaRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void save(Lemma lemma) {
        repo.upsertLemma(lemma.getSite().getId(),
                lemma.getLemma(),
                lemma.getFrequency(),
                lemma.getFrequency());
    }
}
