package com.gardle.domain;

import com.gardle.domain.enumeration.LeasingStatus;
import lombok.Data;
import org.hibernate.annotations.Where;
import org.hibernate.search.annotations.Field;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Leasing.
 */
@Entity
@Data
@Table(name = "leasing")
@Where(clause = "deleted IS NULL")
public class Leasing extends AbstractDeletableAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "leasing_sequenceGenerator")
    @SequenceGenerator(name = "leasing_sequenceGenerator")
    private Long id;

    @Column(name = "from_time")
    @NotNull
    @Field
    private Instant from;

    @Column(name = "to_time")
    @NotNull
    @Field
    private Instant to;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @NotNull
    private LeasingStatus status;

    @ManyToMany
    @JoinTable(name = "leasing_message",
        joinColumns = @JoinColumn(name = "leasing_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "message_id", referencedColumnName = "id"))
    private Set<Message> messages = new HashSet<>();

    @ManyToOne
    @NotNull
    private User user;

    @ManyToOne
    @NotNull
    private GardenField gardenField;

    @NotNull
    private String paymentSessionId;

    public Leasing from(Instant from) {
        this.from = from;
        return this;
    }

    public Leasing to(Instant to) {
        this.to = to;
        return this;
    }

    public Leasing status(LeasingStatus status) {
        this.status = status;
        return this;
    }

    public Leasing messages(Set<Message> messages) {
        this.messages = messages;
        return this;
    }

    public Leasing addMessage(Message message) {
        this.messages.add(message);
        message.setLeasing(this);
        return this;
    }

    public Leasing gardenField(GardenField gardenField) {
        this.gardenField = gardenField;
        return this;
    }

    public Leasing user(User user) {
        this.user = user;
        return this;
    }

    public Leasing paymentSessionId(String paymentSessionId) {
        this.paymentSessionId = paymentSessionId;
        return this;
    }

    public Long getPeriodInDays() {
        return ChronoUnit.DAYS.between(from, to.plus(1, ChronoUnit.DAYS));
    }

    public Integer getPriceSumInCents() {
        return (int) (this.getGardenField().getSizeInM2() * this.getGardenField().getPricePerM2()
            * this.getPeriodInDays() * 100);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Leasing leasing = (Leasing) o;
        return Objects.equals(id, leasing.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
