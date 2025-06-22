package searchengine.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Set.of;

@Getter
@Setter
@Entity
@Table(name = "site")
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    @OneToMany(mappedBy = "site")
    private Set<Page> pages = of();

    @OneToMany(mappedBy = "site")
    private Set<Lemma> lemmas = of();
}