package app.entities;

import app.enums.FormatCategory;
import app.enums.SubCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resource")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private Integer learningId;

    private String learningResourceLink;

    @Enumerated(EnumType.STRING)
    private FormatCategory formatCategory;

    @Enumerated(EnumType.STRING)
    private SubCategory subCategory;

    private String description;


    @ManyToOne
    @JoinColumn(name = "contributor_id", nullable = false)
    @ToString.Exclude
    private Contributor contributor;


    public Resource(Integer learningId, String learningResourceLink, FormatCategory formatCategory, SubCategory subCategory, String description, Contributor contributor) {
        this.learningId = learningId;
        this.learningResourceLink = learningResourceLink;
        this.formatCategory = formatCategory;
        this.subCategory = subCategory;
        this.description = description;
        this.contributor = contributor;
    }

}
