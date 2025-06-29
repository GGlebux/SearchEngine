package engine.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Getter
@Setter
@Entity
@Table(name = "page")
@NoArgsConstructor
public class Page {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "code", nullable = false)
    private Integer code;

    @Lob
    @Column(name = "content", nullable = false,  columnDefinition = "MEDIUMTEXT")
    private String content;

    public Page(Site site, String path, Integer code, String content) {
        this.site = site;
        this.path = path;
        this.code = code;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Page{" +
                "path='" + path + '\'' +
                ", code=" + code +
                '}';
    }
}