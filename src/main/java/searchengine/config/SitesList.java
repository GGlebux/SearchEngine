package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.List.of;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {
    private List<SiteUrl> siteUrls = of();
}
