package com.gardle.service.dto;

import com.gardle.config.Constants;
import com.gardle.domain.Authority;
import com.gardle.domain.User;
import com.gardle.validator.ValidIBAN;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 */
@Data
public class UserDTO {

    private Long id;

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    private boolean activated = false;

    private LocalDate birthDate;

    @AssertTrue
    private boolean isOlderThan18() {
        return birthDate == null || birthDate.isBefore(LocalDate.now().minusYears(18));
    }

    @Pattern(regexp = Constants.TELEPHONE_REGEX)
    private String tel;

    @Size(min = 2, max = 10)
    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Set<String> authorities;

    @NotNull
    @ValidIBAN
    private String bankAccountIBAN;

    private Boolean stripeAccountVerified;

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.birthDate = user.getBirthDate();
        this.tel = user.getTel();
        this.activated = user.isActivated();
        this.langKey = user.getLangKey();
        this.createdBy = user.getCreatedBy();
        this.createdDate = user.getCreatedDate();
        this.lastModifiedBy = user.getLastModifiedBy();
        this.lastModifiedDate = user.getLastModifiedDate();
        this.authorities = user.getAuthorities().stream()
            .map(Authority::getName)
            .collect(Collectors.toSet());
        this.bankAccountIBAN = user.getBankAccountIBAN();
        this.stripeAccountVerified = user.getStripeAccountVerified();
        this.bankAccountIBAN = user.getBankAccountIBAN();
    }
}
