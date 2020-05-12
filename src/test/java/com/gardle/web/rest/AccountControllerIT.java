package com.gardle.web.rest;

import com.gardle.GardleApp;
import com.gardle.config.Constants;
import com.gardle.domain.Authority;
import com.gardle.domain.User;
import com.gardle.repository.AuthorityRepository;
import com.gardle.repository.UserRepository;
import com.gardle.security.AuthoritiesConstants;
import com.gardle.service.MailService;
import com.gardle.service.UserService;
import com.gardle.service.dto.PasswordChangeDTO;
import com.gardle.service.dto.UserDTO;
import com.gardle.web.rest.errors.ExceptionTranslator;
import com.gardle.web.rest.vm.KeyAndPasswordVM;
import com.gardle.web.rest.vm.ManagedUserVM;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link AccountController} REST controller.
 */
@SpringBootTest(classes = GardleApp.class)
public class AccountControllerIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpMessageConverter<?>[] httpMessageConverters;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Mock
    private UserService mockUserService;

    @Mock
    private MailService mockMailService;

    private MockMvc restMvc;

    private MockMvc restUserMockMvc;
    private static final String DEFAULT_IBAN = "AT89370400440532013000";


    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendActivationEmail(any());
        AccountController accountController =
            new AccountController(userRepository, userService, mockMailService);

        AccountController accountUserMockResource =
            new AccountController(userRepository, mockUserService, mockMailService);
        this.restMvc = MockMvcBuilders.standaloneSetup(accountController)
            .setMessageConverters(httpMessageConverters)
            .setControllerAdvice(exceptionTranslator)
            .build();
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource)
            .setControllerAdvice(exceptionTranslator)
            .build();
    }

    @Test
    public void testNonAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/v1/authenticate")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    public void testAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/v1/authenticate")
            .with(request -> {
                request.setRemoteUser("test");
                return request;
            })
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("test"));
    }

    @Test
    public void testGetExistingAccount() throws Exception {
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.ADMIN);
        authorities.add(authority);

        User user = new User();
        user.setLogin("test");
        user.setFirstName("john");
        user.setLastName("doe");
        user.setEmail("john.doe@jhipster.com");
        user.setBirthDate(LocalDate.of(1990, 1, 2));
        user.setTel("067761244368");
        user.setLangKey("en");
        user.setAuthorities(authorities);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        when(mockUserService.getUserWithAuthorities()).thenReturn(Optional.of(user));

        restUserMockMvc.perform(get("/api/v1/account")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value("test"))
            .andExpect(jsonPath("$.firstName").value("john"))
            .andExpect(jsonPath("$.lastName").value("doe"))
            .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("$.langKey").value("en"))
            .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN));
    }

    @Test
    public void testGetUnknownAccount() throws Exception {
        when(mockUserService.getUserWithAuthorities()).thenReturn(Optional.empty());

        restUserMockMvc.perform(get("/api/v1/account")
            .accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void testRegisterValid() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("test-register-valid");
        validUser.setPassword("password");
        validUser.setFirstName("Alice");
        validUser.setLastName("Test");
        validUser.setEmail("test-register-valid@example.com");
        validUser.setBirthDate(LocalDate.of(1990, 1, 2));
        validUser.setTel("067761244368");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        validUser.setBankAccountIBAN(DEFAULT_IBAN);
        assertThat(userRepository.findOneByLogin("test-register-valid").isPresent()).isFalse();

        restMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        assertThat(userRepository.findOneByLogin("test-register-valid").isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testRegisterInvalidLogin() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("funky-log!n");// <-- invalid
        invalidUser.setPassword("password");
        invalidUser.setFirstName("Funky");
        invalidUser.setLastName("One");
        invalidUser.setEmail("funky@example.com");
        invalidUser.setActivated(true);
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        invalidUser.setBankAccountIBAN(DEFAULT_IBAN);

        restUserMockMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByEmailIgnoreCase("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidEmail() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword("password");
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("invalid");// <-- invalid
        invalidUser.setActivated(true);
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        invalidUser.setBankAccountIBAN(DEFAULT_IBAN);

        restUserMockMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidPassword() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword("123");// password with only 3 digits
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("bob@example.com");
        invalidUser.setActivated(true);
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        invalidUser.setBankAccountIBAN(DEFAULT_IBAN);

        restUserMockMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidIBAN() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword("password");
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("test@mail.com");
        invalidUser.setActivated(true);
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        invalidUser.setBankAccountIBAN("invalidIBAN123");

        restUserMockMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterNullPassword() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword(null);// invalid null password
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("bob@example.com");
        invalidUser.setActivated(true);
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        invalidUser.setBankAccountIBAN(DEFAULT_IBAN);

        restUserMockMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateLogin() throws Exception {
        // First registration
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setLogin("alice");
        firstUser.setPassword("password");
        firstUser.setFirstName("Alice");
        firstUser.setLastName("Something");
        firstUser.setEmail("alice@example.com");
        firstUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        firstUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        firstUser.setBankAccountIBAN(DEFAULT_IBAN);

        // Duplicate login, different email
        ManagedUserVM secondUser = new ManagedUserVM();
        secondUser.setLogin(firstUser.getLogin());
        secondUser.setPassword(firstUser.getPassword());
        secondUser.setFirstName(firstUser.getFirstName());
        secondUser.setLastName(firstUser.getLastName());
        secondUser.setEmail("alice2@example.com");
        secondUser.setLangKey(firstUser.getLangKey());
        secondUser.setCreatedBy(firstUser.getCreatedBy());
        secondUser.setCreatedDate(firstUser.getCreatedDate());
        secondUser.setLastModifiedBy(firstUser.getLastModifiedBy());
        secondUser.setLastModifiedDate(firstUser.getLastModifiedDate());
        secondUser.setAuthorities(new HashSet<>(firstUser.getAuthorities()));
        secondUser.setBankAccountIBAN(DEFAULT_IBAN);

        // First user
        restMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(firstUser)))
            .andExpect(status().isCreated());

        // Second (non activated) user
        restMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().isCreated());

        Optional<User> testUser = userRepository.findOneByEmailIgnoreCase("alice2@example.com");
        assertThat(testUser.isPresent()).isTrue();
        testUser.get().setActivated(true);
        userRepository.saveAndFlush(testUser.get());

        // Second (already activated) user
        restMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    public void testRegisterDuplicateEmail() throws Exception {
        // First user
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setLogin("test-register-duplicate-email");
        firstUser.setPassword("password");
        firstUser.setFirstName("Alice");
        firstUser.setLastName("Test");
        firstUser.setEmail("test-register-duplicate-email@example.com");
        firstUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        firstUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        firstUser.setBankAccountIBAN(DEFAULT_IBAN);


        // Register first user
        restMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(firstUser)))
            .andExpect(status().isCreated());

        Optional<User> testUser1 = userRepository.findOneByLogin("test-register-duplicate-email");
        assertThat(testUser1.isPresent()).isTrue();

        // Duplicate email, different login
        ManagedUserVM secondUser = new ManagedUserVM();
        secondUser.setLogin("test-register-duplicate-email-2");
        secondUser.setPassword(firstUser.getPassword());
        secondUser.setFirstName(firstUser.getFirstName());
        secondUser.setLastName(firstUser.getLastName());
        secondUser.setEmail(firstUser.getEmail());
        secondUser.setLangKey(firstUser.getLangKey());
        secondUser.setAuthorities(new HashSet<>(firstUser.getAuthorities()));
        secondUser.setBankAccountIBAN(DEFAULT_IBAN);

        // Register second (non activated) user
        restMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().isCreated());

        Optional<User> testUser2 = userRepository.findOneByLogin("test-register-duplicate-email");
        assertThat(testUser2.isPresent()).isFalse();

        Optional<User> testUser3 = userRepository.findOneByLogin("test-register-duplicate-email-2");
        assertThat(testUser3.isPresent()).isTrue();

        // Duplicate email - with uppercase email address
        ManagedUserVM userWithUpperCaseEmail = new ManagedUserVM();
        userWithUpperCaseEmail.setId(firstUser.getId());
        userWithUpperCaseEmail.setLogin("test-register-duplicate-email-3");
        userWithUpperCaseEmail.setPassword(firstUser.getPassword());
        userWithUpperCaseEmail.setFirstName(firstUser.getFirstName());
        userWithUpperCaseEmail.setLastName(firstUser.getLastName());
        userWithUpperCaseEmail.setEmail("TEST-register-duplicate-email@example.com");
        userWithUpperCaseEmail.setLangKey(firstUser.getLangKey());
        userWithUpperCaseEmail.setAuthorities(new HashSet<>(firstUser.getAuthorities()));
        userWithUpperCaseEmail.setBankAccountIBAN(DEFAULT_IBAN);

        // Register third (not activated) user
        restMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userWithUpperCaseEmail)))
            .andExpect(status().isCreated());

        Optional<User> testUser4 = userRepository.findOneByLogin("test-register-duplicate-email-3");
        assertThat(testUser4.isPresent()).isTrue();
        assertThat(testUser4.get().getEmail()).isEqualTo("test-register-duplicate-email@example.com");

        testUser4.get().setActivated(true);
        userService.updateUser((new UserDTO(testUser4.get())));

        // Register 4th (already activated) user
        restMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    public void testRegisterAdminIsIgnored() throws Exception {
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.USER);
        authorityRepository.saveAndFlush(authority);

        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("badguy");
        validUser.setPassword("password");
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@Wennexample.com");
        validUser.setActivated(true);
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setBankAccountIBAN(DEFAULT_IBAN);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));
        validUser.setBankAccountIBAN(DEFAULT_IBAN);

        restMvc.perform(
            post("/api/v1/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());
        Optional<User> userDup = userRepository.findOneByLogin("badguy");
        assertThat(userDup.isPresent()).isTrue();
        assertThat(userDup.get().getAuthorities()).hasSize(1)
            .containsExactly(authorityRepository.findById(AuthoritiesConstants.USER).get());
    }

    @Test
    @Transactional
    public void testActivateAccount() throws Exception {
        final String activationKey = "some activation key";
        User user = new User();
        user.setLogin("activate-account");
        user.setEmail("activate-account@example.com");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(false);
        user.setActivationKey(activationKey);
        user.setBankAccountIBAN(DEFAULT_IBAN);

        userRepository.saveAndFlush(user);

        restMvc.perform(get("/api/v1/activate?key={activationKey}", activationKey))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk());

        user = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(Objects.requireNonNull(user).isActivated()).isTrue();
    }

    @Test
    @Transactional
    public void testActivateAccountWithWrongKey() throws Exception {
        restMvc.perform(get("/api/v1/activate?key=wrongActivationKey"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser("save-account")
    public void testSaveAccount() throws Exception {
        User user = new User();
        user.setLogin("save-account");
        user.setEmail("save-account@example.com");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(user);

        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-account@example.com");
        userDTO.setActivated(false);
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));
        userDTO.setBankAccountIBAN(DEFAULT_IBAN);

        restMvc.perform(
            post("/api/v1/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(Objects.requireNonNull(updatedUser).getFirstName()).isEqualTo(userDTO.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(userDTO.getLastName());
        assertThat(updatedUser.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(updatedUser.getLangKey()).isEqualTo(userDTO.getLangKey());
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(updatedUser.isActivated()).isEqualTo(true);
        assertThat(updatedUser.getAuthorities()).isEmpty();
    }

    @Test
    @Transactional
    @WithMockUser("save-invalid-email")
    public void testSaveInvalidEmail() throws Exception {
        User user = new User();
        user.setLogin("save-invalid-email");
        user.setEmail("save-invalid-email@example.com");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(user);

        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("invalid email");
        userDTO.setActivated(false);
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));
        userDTO.setBankAccountIBAN(DEFAULT_IBAN);
        restMvc.perform(
            post("/api/v1/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isBadRequest());

        assertThat(userRepository.findOneByEmailIgnoreCase("invalid email")).isNotPresent();
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email")
    public void testSaveExistingEmail() throws Exception {
        User user = new User();
        user.setLogin("save-existing-email");
        user.setEmail("save-existing-email@example.com");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(user);

        User anotherUser = new User();
        anotherUser.setLogin("save-existing-email2");
        anotherUser.setEmail("save-existing-email2@example.com");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(anotherUser);

        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-existing-email2@example.com");
        userDTO.setActivated(false);
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));
        userDTO.setBankAccountIBAN(DEFAULT_IBAN);
        restMvc.perform(
            post("/api/v1/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isConflict());

        User updatedUser = userRepository.findOneByLogin("save-existing-email").orElse(null);
        assertThat(Objects.requireNonNull(updatedUser).getEmail()).isEqualTo("save-existing-email@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email-and-login")
    public void testSaveExistingEmailAndLogin() throws Exception {
        User user = new User();
        user.setLogin("save-existing-email-and-login");
        user.setEmail("save-existing-email-and-login@example.com");
        user.setBirthDate(LocalDate.of(1990, 1, 2));
        user.setTel("067761244368");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(user);

        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("save-existing-email-and-login");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-existing-email-and-login@example.com");
        userDTO.setBirthDate(LocalDate.of(1990, 1, 2));
        userDTO.setTel("067761244368");
        userDTO.setActivated(false);
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));
        userDTO.setBankAccountIBAN(DEFAULT_IBAN);
        restMvc.perform(
            post("/api/v1/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("save-existing-email-and-login").orElse(null);
        assertThat(Objects.requireNonNull(updatedUser).getEmail()).isEqualTo("save-existing-email-and-login@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("change-password-wrong-existing-password")
    public void testChangePasswordWrongExistingPassword() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-wrong-existing-password");
        user.setEmail("change-password-wrong-existing-password@example.com");
        user.setBankAccountIBAN(DEFAULT_IBAN);

        userRepository.saveAndFlush(user);

        restMvc.perform(post("/api/v1/account/change-password")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO("1" + currentPassword, "new password"))))
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-wrong-existing-password").orElse(null);
        assertThat(passwordEncoder.matches("new password", Objects.requireNonNull(updatedUser).getPassword())).isFalse();
        assertThat(passwordEncoder.matches(currentPassword, updatedUser.getPassword())).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser("change-password")
    public void testChangePassword() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password");
        user.setEmail("change-password@example.com");
        user.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(user);

        restMvc.perform(post("/api/v1/account/change-password")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, "new password"))))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin("change-password").orElse(null);
        assertThat(passwordEncoder.matches("new password", Objects.requireNonNull(updatedUser).getPassword())).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-small")
    public void testChangePasswordTooSmall() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-too-small");
        user.setEmail("change-password-too-small@example.com");
        user.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(user);

        String newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MIN_LENGTH - 1);

        restMvc.perform(post("/api/v1/account/change-password")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, newPassword))))
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-too-small").orElse(null);
        assertThat(Objects.requireNonNull(updatedUser).getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-long")
    public void testChangePasswordTooLong() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-too-long");
        user.setEmail("change-password-too-long@example.com");
        user.setBankAccountIBAN(DEFAULT_IBAN);

        userRepository.saveAndFlush(user);

        String newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MAX_LENGTH + 1);

        restMvc.perform(post("/api/v1/account/change-password")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, newPassword))))
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-too-long").orElse(null);
        assertThat(Objects.requireNonNull(updatedUser).getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @Transactional
    @WithMockUser("change-password-empty")
    public void testChangePasswordEmpty() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-empty");
        user.setEmail("change-password-empty@example.com");
        user.setBankAccountIBAN(DEFAULT_IBAN);

        userRepository.saveAndFlush(user);

        restMvc.perform(post("/api/v1/account/change-password")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, ""))))
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin("change-password-empty").orElse(null);
        assertThat(Objects.requireNonNull(updatedUser).getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @Transactional
    public void testRequestPasswordReset() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setLogin("password-reset");
        user.setEmail("password-reset@example.com");
        user.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(user);

        restMvc.perform(post("/api/v1/account/reset-password/init")
            .content("password-reset@example.com"))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void testRequestPasswordResetUpperCaseEmail() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setLogin("password-reset");
        user.setEmail("password-reset@example.com");
        user.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(user);

        restMvc.perform(post("/api/v1/account/reset-password/init")
            .content("password-reset@EXAMPLE.COM"))
            .andExpect(status().isOk());
    }

    @Test
    public void testRequestPasswordResetWrongEmail() throws Exception {
        restMvc.perform(
            post("/api/v1/account/reset-password/init")
                .content("password-reset-wrong-email@example.com"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void testFinishPasswordReset() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setLogin("finish-password-reset");
        user.setEmail("finish-password-reset@example.com");
        user.setPasswordResetTimestamp(Instant.now().plusSeconds(60));
        user.setResetKey("reset key");
        user.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("new password");

        restMvc.perform(
            post("/api/v1/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
            .andExpect(status().isOk());

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), Objects.requireNonNull(updatedUser).getPassword())).isTrue();
    }

    @Test
    @Transactional
    public void testFinishPasswordResetTooSmall() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setLogin("finish-password-reset-too-small");
        user.setEmail("finish-password-reset-too-small@example.com");
        user.setPasswordResetTimestamp(Instant.now().plusSeconds(60));
        user.setResetKey("reset key too small");
        user.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("foo");

        restMvc.perform(
            post("/api/v1/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
            .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), Objects.requireNonNull(updatedUser).getPassword())).isFalse();
    }


    @Test
    @Transactional
    public void testFinishPasswordResetWrongKey() throws Exception {
        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("wrong reset key");
        keyAndPassword.setNewPassword("new password");

        restMvc.perform(
            post("/api/v1/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
            .andExpect(status().isNotFound());
    }
}
