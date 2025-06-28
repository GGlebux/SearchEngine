package searchengine.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.time.Instant.now;
import static java.util.Objects.hash;
import static java.util.Set.of;
import static org.hibernate.annotations.CascadeType.ALL;

@Getter
@Setter
@Entity
@Table(name = "site")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Site {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "status_time", nullable = false)
    private Instant statusTime;

    @Lob
    @Column(name = "last_error")
    private String lastError;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "site", orphanRemoval = true)
    @Cascade(ALL)
    private Set<Page> pages = of();

    @OneToMany(mappedBy = "site")
    @Cascade(ALL)
    private Set<Lemma> lemmas = of();

    public Site(String name, String url, Status status, String lastError) {
        this.name = name;
        this.url = url;
        this.status = status;
        this.statusTime = now();
        this.lastError = lastError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Site site = (Site) o;
        return Objects.equals(id, site.id) && Objects.equals(url, site.url);
    }

    @Override
    public int hashCode() {
        return hash(id, url);
    }
}