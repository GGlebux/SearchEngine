package engine.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.annotations.OnDeleteAction.NO_ACTION;

@Data
@Getter
@NoArgsConstructor
@Entity
@Table(name = "lemma")
public class Lemma {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY, optional = false)
    @OnDelete(action = NO_ACTION)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "lemma", nullable = false)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private Long frequency;

    public Lemma(Site site, String lemma, Long frequency) {
        this.site = site;
        this.lemma = lemma;
        this.frequency = frequency;
    }
}