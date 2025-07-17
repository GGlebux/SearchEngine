package engine.parsing;

import engine.models.Site;
import engine.morph.Lemmatizator;
import engine.services.LemmaService;
import engine.services.PageService;
import engine.services.SiteService;
import engine.services.VisitedLinksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParsingTaskFactory {
    private final PageService pageService;
    private final SiteService siteService;
    private final VisitedLinksService visitedLinksService;
    private final LemmaService lemmasService;
    private final Parser parser;
    private final Lemmatizator lemmatizator;

    @Autowired
    public ParsingTaskFactory(PageService pageService, SiteService siteService, VisitedLinksService visitedLinksService, LemmaService lemmasService, Parser parserFactory1, Lemmatizator lemmatizator) {
        this.pageService = pageService;
        this.siteService = siteService;
        this.visitedLinksService = visitedLinksService;
        this.lemmasService = lemmasService;
        this.parser = parserFactory1;
        this.lemmatizator = lemmatizator;
    }

    public ParsingTask createParsingTask(Site domain, String url) {
        return new ParsingTask(domain,
                url,
                this,
                parser,
                pageService,
                siteService,
                lemmasService,
                visitedLinksService,
                lemmatizator);
    }
}
