package com.gardle.service;

import com.gardle.config.Constants;
import com.gardle.domain.Authority;
import com.gardle.domain.User;
import com.gardle.repository.AuthorityRepository;
import com.gardle.repository.UserRepository;
import com.gardle.security.AuthoritiesConstants;
import com.gardle.security.SecurityUtils;
import com.gardle.service.dto.GetStripeAccountLinkUrlDTO;
import com.gardle.service.dto.UpdateStripeAccountVerifiedUserDTO;
import com.gardle.service.dto.UserDTO;
import com.gardle.service.exception.*;
import com.gardle.service.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    private final MailService mailService;

    private final PaymentService paymentService;

    private final SecurityHelperService securityHelperService;

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                user.setStripeAccountId(paymentService.createPaymentAccount(user.getEmail(), user.getBankAccountIBAN()));
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository.findOneByResetKey(key)
            .filter(user -> user.getPasswordResetTimestamp().isAfter(Instant.now().minusSeconds(86400)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setPasswordResetTimestamp(null);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmailIgnoreCase(mail)
            .filter(User::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setPasswordResetTimestamp(Instant.now());
                return user;
            });
    }

    public User registerUser(UserDTO userDTO, String password) {
        checkUniqueUserDataViolations(userDTO);

        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        newUser.setEmail(userDTO.getEmail().toLowerCase());
        newUser.setBirthDate(userDTO.getBirthDate());
        newUser.setTel(userDTO.getTel());
        newUser.setLangKey(userDTO.getLangKey());
        newUser.setBankAccountIBAN(userDTO.getBankAccountIBAN());
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    private void checkUniqueUserDataViolations(final UserDTO userDTO) {
        checkIfUserEmailAlreadyExists(userDTO);
        checkIfUserLoginAlreadyExists(userDTO);
    }

    private void checkIfUserEmailAlreadyExists(final UserDTO userDTO) {
        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed && existingUser.getDeleted() == null) {
                throw new EmailAlreadyUsedServiceException();
            }
        });
    }

    private void checkIfUserLoginAlreadyExists(final UserDTO userDTO) {
        userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed && existingUser.getDeleted() == null) {
                throw new UsernameAlreadyUsedServiceException();
            }
        });
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        return true;
    }

    public User createUser(UserDTO userDTO) {
        checkUniqueUserDataViolations(userDTO);

        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail().toLowerCase());
        user.setBirthDate(userDTO.getBirthDate());
        user.setTel(userDTO.getTel());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setPasswordResetTimestamp(Instant.now());
        user.setActivated(true);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO.getAuthorities().stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        user.setBankAccountIBAN(userDTO.getBankAccountIBAN());
        user.setStripeAccountId(paymentService.createPaymentAccount(user.getEmail(), user.getBankAccountIBAN()));
        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     */
    public void updateCurrentUser(String firstName, String lastName, String email, String langKey,
                                  LocalDate birthDate, String tel, String login) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setLangKey(langKey);
                user.setBirthDate(birthDate);
                user.setTel(tel);
                user.setEmail(email.toLowerCase());
                user.setLogin(login);

                log.debug("Changed Information for User: {}", user);
            });
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        return Optional.of(userRepository
            .findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setEmail(userDTO.getEmail().toLowerCase());
                user.setBirthDate(userDTO.getBirthDate());
                user.setTel(userDTO.getTel());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO.getAuthorities().stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(UserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            user.setDeleted(Instant.now());
            // so that it can be reused -> re-registration
            user.setEmail(UUID.randomUUID().toString() + "@localhost");
            // so that it can be reused -> re-registration
            user.setLogin(UUID.randomUUID().toString());
            userRepository.save(user);
            log.debug("Deleted User: {}", user);
        });
    }

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordServiceException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                log.debug("Changed password for User: {}", user);
                mailService.sendPasswordChangedMail(user);
            });
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(Long id) {
        return userRepository.findOneWithAuthoritiesById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                log.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     *
     * @return a list of all the authorities.
     */
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

    public Optional<UserDTO> getUserByStripeAccountId(final String stripeAccountId) {
        return Optional.of(this.userRepository.findByStripeAccountId(stripeAccountId));
    }

    public UserDTO updateStripeActivationForCurrentUser(final UpdateStripeAccountVerifiedUserDTO userDTO) {
        User currentUser = SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin).orElse(null);
        if (currentUser == null) {
            throw new NotLoggedInServiceException();
        }
        if (userDTO == null || userDTO.getVerified() == null) {
            log.debug("dto or activated null, nothing to update");
            return new UserDTO(currentUser);
        }
        if (StringUtils.isEmpty(currentUser.getStripeVerificationKey()) ||
            !currentUser.getStripeVerificationKey().equals(userDTO.getStripeVerificationKey())) {
            log.debug("Cannot update verification status of user {}, received verification key {} " +
                    "is empty or does not match verification key of user: {}",
                currentUser, userDTO.getStripeVerificationKey(), currentUser.getStripeVerificationKey());
            throw new StripeVerificationKeyValidationServiceException("Validation of stripe verification key failed", null);
        }

        currentUser.setStripeAccountVerified(userDTO.getVerified());
        log.debug("Updating user: {}, to have stripe verification {}", currentUser, userDTO.getVerified());
        return new UserDTO(userRepository.save(currentUser));
    }

    public String getAccountLinkUrl() {
        String verificationKey = RandomUtil.generateStripeVerificationKey();
        User currentUser = securityHelperService.getLoggedInUser();
        currentUser.setStripeVerificationKey(verificationKey);
        this.updateUser(new UserDTO(currentUser));

        return this.paymentService.getAccountLinkUrl(
            new GetStripeAccountLinkUrlDTO(currentUser.getStripeAccountId(), currentUser.getStripeVerificationKey()));
    }
}
