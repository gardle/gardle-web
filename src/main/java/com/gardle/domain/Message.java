package com.gardle.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gardle.domain.enumeration.MessageType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * A Message.
 */
@Data
@Entity
@Table(name = "message")
@NoArgsConstructor
@Where(clause = "deleted IS NULL")
public class Message extends AbstractDeletableAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "content", length = 2000)
    @SafeHtml
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MessageType type;

    @ManyToOne
    private Leasing leasing;

    @ManyToOne
    private User userFrom;

    @ManyToOne
    @NonNull
    private User userTo;

    private UUID thread;

    @Column(name = "created_date", updatable = false)
    @JsonIgnore
    private Instant createdDate = Instant.now();

    private Boolean opened;

    public Message content(String content) {
        this.content = content;
        return this;
    }

    public Message type(MessageType type) {
        this.type = type;
        return this;
    }

}
