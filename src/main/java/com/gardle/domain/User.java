package com.gardle.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gardle.config.Constants;
import com.gardle.validator.ValidIBAN;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A user.
 */
@Entity
@Table(name = "gardle_user")
@Data
@Where(clause = "deleted IS NULL")
public class User extends AbstractDeletableAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_sequenceGenerator")
    @SequenceGenerator(name = "user_sequenceGenerator")
    private Long id;

    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true)
    private String email;

    private LocalDate birthDate;

    @AssertTrue
    private boolean isOlderThan18() {
        return birthDate == null || birthDate.isBefore(LocalDate.now().minusYears(18));
    }

    @Pattern(regexp = Constants.TELEPHONE_REGEX)
    @Size(min = 4)
    @Column(length = 128)
    private String tel;

    private String stripeAccountId;

    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    @Size(min = 2, max = 10)
    @Column(name = "lang_key", length = 10)
    private String langKey;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    @JsonIgnore
    private String resetKey;

    @Column(name = "reset_date")
    private Instant passwordResetTimestamp = null;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "user_authority",
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")})

    @BatchSize(size = 20)
    private Set<Authority> authorities = new HashSet<>();

    @NotNull
    @ValidIBAN
    private String bankAccountIBAN;

    private Boolean stripeAccountVerified = false;
    private String stripeVerificationKey;

    public void setId(Long id) {
        this.id = id;
    }

    // Lowercase the login before saving it in database
    public void setLogin(String login) {
        this.login = StringUtils.lowerCase(login, Locale.ENGLISH);
    }
}
