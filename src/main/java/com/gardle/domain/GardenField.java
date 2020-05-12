package com.gardle.domain;

import lombok.Data;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.annotations.Where;
import org.hibernate.search.annotations.*;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Data
@Indexed
@Spatial(spatialMode = SpatialMode.HASH) //better performance, data distribution independant but index gets larger, see hibernate search docs
/*
  for now this analyzer uses the standard tokenizer which splits words at hyphen punctuation, etc
  then a lower case filter is applied, mapping all letters to lowercase and the snowballporterfilter
  which stems words e.g.
 */
@AnalyzerDefs({
    @AnalyzerDef(name = "descriptionAnalyzer",
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
            @TokenFilterDef(factory = StandardFilterFactory.class),
            @TokenFilterDef(factory = LowerCaseFilterFactory.class)
        })
})
@Where(clause = "deleted IS NULL")
public class GardenField extends AbstractDeletableAuditingEntity implements Serializable {
    @Transient
    private static final Double PRIZE_CALCULATION_PERIOD_IN_DAYS = 30.0;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gardenField_sequenceGenerator")
    @SequenceGenerator(name = "gardenField_sequenceGenerator")
    private Long id;

    @NotBlank
    @Fields({
        @Field(name = "name")
    })
    @Size(max = 100)
    private String name;

    @Positive
    @NotNull
    @Field
    @NumericField(forField = "sizeInM2")
    private Double sizeInM2;

    @PositiveOrZero
    @NotNull
    @Field
    @NumericField(forField = "pricePerM2")
    private Double pricePerM2;

    @NotNull
    @Latitude
    private Double latitude;

    @NotNull
    @Longitude
    private Double longitude;

    @Field
    private Boolean roofed;

    @Field
    private Boolean glassHouse;

    @Field
    private Boolean high;

    @Field
    private Boolean water;

    @Field
    private Boolean electricity;

    @PositiveOrZero
    @Field
    @NumericField(forField = "phValue")
    private Double phValue;

    @Size(max = 1000)
    @Field
    @Analyzer(definition = "descriptionAnalyzer")
    @SafeHtml
    private String description;

    @NotNull
    @Fields({
        @Field(name = "city")
    })
    private String city;

    @ManyToOne
    @NotNull
    private User owner;

    private String coverImage;

    @ManyToMany
    @JoinTable(name = "neighbours",
        joinColumns = @JoinColumn(name = "gardenFieldId"),
        inverseJoinColumns = @JoinColumn(name = "neighbourGardenFieldId")
    )
    private List<User> neighbours;

    @ManyToMany
    @JoinTable(name = "neighbours_of",
        joinColumns = @JoinColumn(name = "neighbourGardenFieldId"),
        inverseJoinColumns = @JoinColumn(name = "gardenFieldId")
    )
    private List<User> neighboursOf;

    @OneToMany
    @IndexedEmbedded(includePaths = {"from", "to"})
    private List<Leasing> leasings;

    // = price for the field per one month period (=30 days)
    @Field
    @Positive
    @NotNull
    private Double pricePerMonth;

    @PrePersist
    @PreUpdate
    private void setPricePerMonth() {
        this.pricePerMonth = this.pricePerM2 * this.sizeInM2 * GardenField.PRIZE_CALCULATION_PERIOD_IN_DAYS;
    }
}
