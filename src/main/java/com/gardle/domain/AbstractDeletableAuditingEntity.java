package com.gardle.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;

/**
 * For entities which will hold the general information of an AuditingEntity but also can be (soft) deleted
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AbstractDeletableAuditingEntity extends AbstractAuditingEntity implements Serializable {
    // see https://docs.jboss.org/hibernate/search/5.10/reference/en-US/html_single/#field-annotation-indexNullAs
    private static final String MAX_LONG_VALUE_FOR_NULL_PLACEHOLDER = "9223372036854775807";
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @Column(name = "deleted")
    @Field(indexNullAs = MAX_LONG_VALUE_FOR_NULL_PLACEHOLDER, analyze = Analyze.NO)
    private Instant deleted = null;
}
