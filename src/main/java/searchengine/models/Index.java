package searchengine.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Getter
@Setter
@Entity
@Table(name = "`index`")
public class Index {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    private Page page;

    @OneToOne
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    private Lemma lemma;

    @Column(name = "`rank`", nullable = false)
    private Double rank;
}