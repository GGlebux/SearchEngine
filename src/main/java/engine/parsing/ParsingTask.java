package engine.parsing;

import engine.models.Lemma;
import engine.morph.Lemmatizator;
import engine.services.LemmaService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import engine.exceptions.ParsingException;
import engine.models.Page;
import engine.models.Site;
import engine.models.Status;
import engine.services.PageService;
import engine.services.SiteService;
import engine.services.VisitedLinksService;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.currentThread;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;
import static engine.models.Status.*;

@AllArgsConstructor
public class ParsingTask extends RecursiveAction {
    private final Site domain;
    private final String root;
    private final ParsingTaskFactory taskFactory;
    private final Parser parser;
    private final PageService pageService;
    private final SiteService siteService;
    private final LemmaService lemmasService;
    private final VisitedLinksService visitedLinks;
    private final Lemmatizator lemmatizator;
    @Getter
    private final AtomicReference<Throwable> error = new AtomicReference<>();
    private final static EnumSet<Status> update_statuses = EnumSet.of(INDEXED, INDEXING);

    @Override
    protected void compute() {
        try {
            if (currentThread().isInterrupted()) return;
            if (visitedLinks.contains(domain.getUrl(), root)) return;
            visitedLinks.add(domain.getUrl(), root);

            PageData data = parser.parseUrl(domain.getUrl(), root);
            pageService.save(new Page(domain, data.getPath(), data.getCode(), data.getContent()));

            Map<String, Integer> lemmas = lemmatizator.collectLemmas(data.getContent());

            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                lemmasService.save(new Lemma(domain, entry.getKey(), entry.getValue()));
            }
            lemmas.forEach((key, value) -> new Lemma(domain, key, value));

            siteService.updateSiteStatus(domain, INDEXING, empty(), update_statuses);

            SECONDS.sleep(1);

            this.createAndInvokeSubtask(data);

        } catch (InterruptedException e) {
            currentThread().interrupt();
        } catch (ParsingException e) {
            this.processingParsingError(e);
        }
    }

    private void createAndInvokeSubtask(PageData data) {
        Set<ParsingTask> subtask = data
                .getLinks()
                .stream()
                .map(link -> taskFactory.createParsingTask(domain, link))
                .collect(toSet());

        if (!currentThread().isInterrupted()) {
            invokeAll(subtask);
        }
    }

    private void processingParsingError(ParsingException e) {
        String message = e.getMessage();
        pageService.save(new Page(domain, e.getPath(), e.getErrorCode(), ""));
        if (root.equals("/")) {
            siteService.updateSiteStatus(domain, FAILED, of(message), update_statuses);
        } else {
            siteService.updateSiteStatus(domain, INDEXING, of(message), update_statuses);
        }
    }
}
