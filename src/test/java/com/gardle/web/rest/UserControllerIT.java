package com.gardle.web.rest;

import com.gardle.GardleApp;
import com.gardle.domain.Authority;
import com.gardle.domain.GardenField;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.LeasingState;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.LeasingRepository;
import com.gardle.repository.UserRepository;
import com.gardle.security.AuthoritiesConstants;
import com.gardle.service.LeasingService;
import com.gardle.service.MailService;
import com.gardle.service.UserService;
import com.gardle.service.dto.UpdateStripeAccountVerifiedUserDTO;
import com.gardle.service.dto.UserDTO;
import com.gardle.service.mapper.UserMapper;
import com.gardle.service.util.RandomUtil;
import com.gardle.web.rest.errors.ExceptionTranslator;
import com.gardle.web.rest.vm.ManagedUserVM;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link UserController} REST controller.
 */
@SpringBootTest(classes = GardleApp.class)
public class UserControllerIT {
    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String UPDATED_LOGIN = "jhipster";
    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_PASSWORD = "passjohndoe";
    private static final String UPDATED_PASSWORD = "passjhipster";
    private static final String DEFAULT_EMAIL = "johndoe@localhost.com";
    private static final String UPDATED_EMAIL = "jhipster@localhost";
    private static final String DEFAULT_TEL = "067761244368";
    private static final LocalDate DEFAULT_BIRTHDATE = LocalDate.of(1990, 1, 2);
    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String UPDATED_FIRSTNAME = "jhipsterFirstName";
    private static final String DEFAULT_LASTNAME = "doe";
    private static final String UPDATED_LASTNAME = "jhipsterLastName";
    private static final String DEFAULT_LANGKEY = "en";
    private static final String UPDATED_LANGKEY = "fr";
    private static final String DEFAULT_IBAN = "AT89370400440532013000";

    private static final String DEFAULT_PAYMENT_SESSION_ID = "cs_test_m5CBqcXSIJeKW7Ijb5vp8D9BrvDlJ6lRn25m6BdTZ0a1cLbIz5xVQ7bX";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GardenFieldRepository gardenFieldRepository;

    @Autowired
    private LeasingRepository leasingRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private LeasingService leasingService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restUserMockMvc;

    private User user;

    @BeforeEach
    public void setup() {
        UserController userController = new UserController(userService, userRepository, mailService, leasingService);

        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .build();
    }

    /**
     * Create a User.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.
     */
    public static User createEntity(EntityManager em) {
        User user = new User();
        user.setLogin(DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5));
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setTel(DEFAULT_TEL);
        user.setBirthDate(DEFAULT_BIRTHDATE);
        user.setLangKey(DEFAULT_LANGKEY);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        return user;
    }

    @BeforeEach
    public void initTest() {
        user = createEntity(em);
        user.setLogin(DEFAULT_LOGIN);
        user.setEmail(DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    public void createUser() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        // Create the User
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setBirthDate(DEFAULT_BIRTHDATE);
        managedUserVM.setTel(DEFAULT_TEL);
        managedUserVM.setEmail(DEFAULT_EMAIL);
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);

        restUserMockMvc.perform(post("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isCreated());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(testUser.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(testUser.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(testUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testUser.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(testUser.getTel()).isEqualTo(DEFAULT_TEL);
        assertThat(testUser.getBirthDate()).isEqualTo(DEFAULT_BIRTHDATE);
        assertThat(testUser.getBankAccountIBAN()).isEqualTo(DEFAULT_IBAN);
    }

    @Test
    @Transactional
    public void createUserWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(1L);
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);
        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc.perform(post("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN);// this login should already be used
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail("anothermail@localhost");
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);
        // Create the User
        restUserMockMvc.perform(post("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isConflict());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithInvalidAge() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN);// this login should already be used
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);
        managedUserVM.setTel(DEFAULT_TEL);
        managedUserVM.setBirthDate(LocalDate.now().minusYears(10));
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Create the User
        restUserMockMvc.perform(post("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithInvalidTelephoneNumber() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN);// this login should already be used
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);
        managedUserVM.setTel("+43abc6363636363");
        managedUserVM.setBirthDate(DEFAULT_BIRTHDATE);
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Create the User
        restUserMockMvc.perform(post("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithExistingEmail() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("anotherlogin");
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);// this email should already be used
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Create the User
        restUserMockMvc.perform(post("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isConflict());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithPreviouslyDeletedUsername() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Delete the user
        restUserMockMvc.perform(delete("/api/v1/users/{login}", user.getLogin())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent());

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN); // The login that is re-used
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail("another@email.com");
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);

        // Create the User
        restUserMockMvc.perform(post("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isCreated());
    }

    @Test
    @Transactional
    public void createUserWithPreviouslyDeletedEmail() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Delete the user
        restUserMockMvc.perform(delete("/api/v1/users/{login}", user.getLogin())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent());

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("another_login");
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL); // The email that is re-used
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);

        // Create the User
        restUserMockMvc.perform(post("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isCreated());
    }

    @Test
    @Transactional
    public void getAllUsers() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get all the users
        restUserMockMvc.perform(get("/api/v1/users?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)));
    }

    @Test
    @Transactional
    public void getUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get the user
        restUserMockMvc.perform(get("/api/v1/users/{login}", user.getLogin()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value(user.getLogin()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.langKey").value(DEFAULT_LANGKEY));
    }

    @Test
    @Transactional
    public void getNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/v1/users/unknown"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin(updatedUser.getLogin());
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setEmail(UPDATED_EMAIL);
        managedUserVM.setActivated(updatedUser
            .isActivated());
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);
        restUserMockMvc.perform(put("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isOk());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
        assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
    }

    @Test
    @Transactional
    public void updateUserLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin(UPDATED_LOGIN);
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setEmail(UPDATED_EMAIL);
        managedUserVM.setActivated(updatedUser.isActivated());
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);

        restUserMockMvc.perform(put("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isOk());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getLogin()).isEqualTo(UPDATED_LOGIN);
        assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
        assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
    }

    @Test
    @Transactional
    public void updateUserExistingEmail() throws Exception {
        // Initialize the database with 2 users
        userRepository.saveAndFlush(user);

        User anotherUser = new User();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setLangKey("en");
        anotherUser.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(anotherUser);

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin(updatedUser.getLogin());
        managedUserVM.setPassword(updatedUser.getPassword());
        managedUserVM.setFirstName(updatedUser.getFirstName());
        managedUserVM.setLastName(updatedUser.getLastName());
        managedUserVM.setEmail("jhipster@localhost");// this email should already be used by anotherUser
        managedUserVM.setActivated(updatedUser.isActivated());
        managedUserVM.setLangKey(updatedUser.getLangKey());
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);

        restUserMockMvc.perform(put("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    public void updateUserExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        User anotherUser = new User();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setLangKey("en");
        anotherUser.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(anotherUser);

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin("jhipster");// this login should already be used by anotherUser
        managedUserVM.setPassword(updatedUser.getPassword());
        managedUserVM.setFirstName(updatedUser.getFirstName());
        managedUserVM.setLastName(updatedUser.getLastName());
        managedUserVM.setEmail(updatedUser.getEmail());
        managedUserVM.setActivated(updatedUser.isActivated());
        managedUserVM.setLangKey(updatedUser.getLangKey());
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        managedUserVM.setBankAccountIBAN(DEFAULT_IBAN);

        restUserMockMvc.perform(put("/api/v1/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    public void deleteUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeDelete = userRepository.findAll().size();

        // Delete the user
        restUserMockMvc.perform(delete("/api/v1/users/{login}", user.getLogin())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void testUserEquals() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(user1.getId());
        assertThat(user1).isEqualTo(user2);
        user2.setId(2L);
        assertThat(user1).isNotEqualTo(user2);
        user1.setId(null);
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void testUserDTOtoUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(DEFAULT_ID);
        userDTO.setLogin(DEFAULT_LOGIN);
        userDTO.setFirstName(DEFAULT_FIRSTNAME);
        userDTO.setLastName(DEFAULT_LASTNAME);
        userDTO.setEmail(DEFAULT_EMAIL);
        userDTO.setActivated(true);
        userDTO.setLangKey(DEFAULT_LANGKEY);
        userDTO.setCreatedBy(DEFAULT_LOGIN);
        userDTO.setLastModifiedBy(DEFAULT_LOGIN);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        User user = userMapper.userDTOToUser(userDTO);
        assertThat(user.getId()).isEqualTo(DEFAULT_ID);
        assertThat(user.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(user.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(user.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(user.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(user.isActivated()).isEqualTo(true);
        assertThat(user.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(user.getCreatedBy()).isNull();
        assertThat(user.getCreatedDate()).isNotNull();
        assertThat(user.getLastModifiedBy()).isNull();
        assertThat(user.getLastModifiedDate()).isNotNull();
        assertThat(user.getAuthorities()).extracting("name").containsExactly(AuthoritiesConstants.USER);
    }

    @Test
    public void testUserToUserDTO() {
        user.setId(DEFAULT_ID);
        user.setCreatedBy(DEFAULT_LOGIN);
        user.setCreatedDate(Instant.now());
        user.setLastModifiedBy(DEFAULT_LOGIN);
        user.setLastModifiedDate(Instant.now());
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.USER);
        authorities.add(authority);
        user.setAuthorities(authorities);

        UserDTO userDTO = userMapper.userToUserDTO(user);

        assertThat(userDTO.getId()).isEqualTo(DEFAULT_ID);
        assertThat(userDTO.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(userDTO.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(userDTO.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(userDTO.isActivated()).isEqualTo(true);
        assertThat(userDTO.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(userDTO.getCreatedBy()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getCreatedDate()).isEqualTo(user.getCreatedDate());
        assertThat(userDTO.getLastModifiedBy()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getLastModifiedDate()).isEqualTo(user.getLastModifiedDate());
        assertThat(userDTO.getAuthorities()).containsExactly(AuthoritiesConstants.USER);
        assertThat(userDTO.toString()).isNotNull();
    }

    @Test
    public void testAuthorityEquals() {
        Authority authorityA = new Authority();
        assertThat(authorityA).isEqualTo(authorityA);
        assertThat(authorityA).isNotEqualTo(null);
        assertThat(authorityA).isNotEqualTo(new Object());
        assertThat(authorityA.hashCode()).isEqualTo(0);
        assertThat(authorityA.toString()).isNotNull();

        Authority authorityB = new Authority();
        assertThat(authorityA).isEqualTo(authorityB);

        authorityB.setName(AuthoritiesConstants.ADMIN);
        assertThat(authorityA).isNotEqualTo(authorityB);

        authorityA.setName(AuthoritiesConstants.USER);
        assertThat(authorityA).isNotEqualTo(authorityB);

        authorityB.setName(AuthoritiesConstants.USER);
        assertThat(authorityA).isEqualTo(authorityB);
        assertThat(authorityA.hashCode()).isEqualTo(authorityB.hashCode());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getLeasingsForUser() throws Exception {
        User user2 = createEntity(null);
        user2.setLogin("user2");
        user2.setEmail("user2@gardle.ga");

        userRepository.save(user);
        userRepository.save(user2);

        GardenField gardenField = createGardenField();

        Leasing leasing1 = new Leasing();
        leasing1.setUser(user);
        leasing1.setGardenField(gardenField);
        leasing1.setFrom(Instant.now().plus(10, ChronoUnit.DAYS));
        leasing1.setTo(Instant.now().plus(20, ChronoUnit.DAYS));
        leasing1.setStatus(LeasingStatus.RESERVED);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing1);

        Leasing leasing2 = new Leasing();
        leasing2.setUser(user2);
        leasing2.setGardenField(gardenField);
        leasing2.setFrom(Instant.now().plus(21, ChronoUnit.DAYS));
        leasing2.setTo(Instant.now().plus(30, ChronoUnit.DAYS));
        leasing2.setStatus(LeasingStatus.RESERVED);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing2);

        Leasing leasing3 = new Leasing();
        leasing3.setUser(user);
        leasing3.setGardenField(gardenField);
        leasing3.setFrom(Instant.now().plus(31, ChronoUnit.DAYS));
        leasing3.setTo(Instant.now().plus(40, ChronoUnit.DAYS));
        leasing3.setStatus(LeasingStatus.RESERVED);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing3);

        assertThat(leasingRepository.findAll()).hasSize(3);

        restUserMockMvc.perform(get("/api/v1/users/{userId}/leasings", user.getId()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(2)))
            .andExpect(jsonPath("$.content.[0].id").value(is(leasing1.getId().intValue())))
            .andExpect(jsonPath("$.content.[1].id").value(is(leasing3.getId().intValue())));
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getLeasingsForUserWithStatusFilter() throws Exception {
        User user2 = createEntity(null);
        user2.setLogin("user2");
        user2.setEmail("user2@gardle.ga");

        userRepository.save(user);

        GardenField gardenField = createGardenField();

        Leasing leasing1 = new Leasing();
        leasing1.setUser(user);
        leasing1.setGardenField(gardenField);
        leasing1.setFrom(Instant.now().plus(10, ChronoUnit.DAYS));
        leasing1.setTo(Instant.now().plus(20, ChronoUnit.DAYS));
        leasing1.setStatus(LeasingStatus.OPEN);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing1);

        Leasing leasing2 = new Leasing();
        leasing2.setUser(user);
        leasing2.setGardenField(gardenField);
        leasing2.setFrom(Instant.now().plus(21, ChronoUnit.DAYS));
        leasing2.setTo(Instant.now().plus(30, ChronoUnit.DAYS));
        leasing2.setStatus(LeasingStatus.RESERVED);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing2);

        Leasing leasing3 = new Leasing();
        leasing3.setUser(user);
        leasing3.setGardenField(gardenField);
        leasing3.setFrom(Instant.now().plus(31, ChronoUnit.DAYS));
        leasing3.setTo(Instant.now().plus(40, ChronoUnit.DAYS));
        leasing3.setStatus(LeasingStatus.CANCELLED);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing3);

        assertThat(leasingRepository.findAll()).hasSize(3);

        restUserMockMvc.perform(get("/api/v1/users/{userId}/leasings?leasingStatus=" + LeasingStatus.OPEN.toString()
            + "&leasingStatus=" + LeasingStatus.CANCELLED, user.getId()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(2)))
            .andExpect(jsonPath("$.content.[0].id").value(is(leasing1.getId().intValue())))
            .andExpect(jsonPath("$.content.[1].id").value(is(leasing3.getId().intValue())));
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getLeasingsForUserWithStatusAndToFilter() throws Exception {
        userRepository.save(user);

        GardenField gardenField = createGardenField();

        Leasing leasing1 = new Leasing();
        leasing1.setUser(user);
        leasing1.setGardenField(gardenField);
        leasing1.setFrom(Instant.now().plus(10, ChronoUnit.DAYS));
        leasing1.setTo(Instant.now().plus(20, ChronoUnit.DAYS));
        leasing1.setStatus(LeasingStatus.OPEN);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing1);

        Leasing leasing2 = new Leasing();
        leasing2.setUser(user);
        leasing2.setGardenField(gardenField);
        leasing2.setFrom(Instant.now().plus(21, ChronoUnit.DAYS));
        leasing2.setTo(Instant.now().plus(30, ChronoUnit.DAYS));
        leasing2.setStatus(LeasingStatus.OPEN);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing2);

        Leasing leasing3 = new Leasing();
        leasing3.setUser(user);
        leasing3.setGardenField(gardenField);
        leasing3.setFrom(Instant.now().plus(31, ChronoUnit.DAYS));
        leasing3.setTo(Instant.now().plus(40, ChronoUnit.DAYS));
        leasing3.setStatus(LeasingStatus.OPEN);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing3);

        assertThat(leasingRepository.findAll()).hasSize(3);

        restUserMockMvc.perform(get("/api/v1/users/{userId}/leasings?leasingStatus=" + LeasingStatus.OPEN.toString()
            + "&to=" + leasing2.getTo().plus(1, ChronoUnit.SECONDS), user.getId()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(2)))
            .andExpect(jsonPath("$.content.[0].id").value(is(leasing1.getId().intValue())))
            .andExpect(jsonPath("$.content.[1].id").value(is(leasing2.getId().intValue())));
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getLeasingsForUserWithStateFilter() throws Exception {
        User user2 = createEntity(null);
        user2.setLogin("user2");
        user2.setEmail("user2@gardle.ga");

        userRepository.save(user);

        GardenField gardenField = createGardenField();

        Leasing leasing1 = new Leasing();
        leasing1.setUser(user);
        leasing1.setGardenField(gardenField);
        leasing1.setFrom(Instant.now().plus(10, ChronoUnit.DAYS));
        leasing1.setTo(Instant.now().plus(20, ChronoUnit.DAYS));
        leasing1.setStatus(LeasingStatus.OPEN);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing1);

        Leasing leasing2 = new Leasing();
        leasing2.setUser(user);
        leasing2.setGardenField(gardenField);
        leasing2.setFrom(Instant.now().plus(21, ChronoUnit.DAYS));
        leasing2.setTo(Instant.now().plus(30, ChronoUnit.DAYS));
        leasing2.setStatus(LeasingStatus.OPEN);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing2);

        Leasing leasing3 = new Leasing();
        leasing3.setUser(user);
        leasing3.setGardenField(gardenField);
        leasing3.setFrom(Instant.now().minus(50, ChronoUnit.DAYS));
        leasing3.setTo(Instant.now().minus(10, ChronoUnit.DAYS));
        leasing3.setStatus(LeasingStatus.OPEN);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing3);

        assertThat(leasingRepository.findAll()).hasSize(3);

        restUserMockMvc.perform(get("/api/v1/users/{userId}/leasings?state=" + LeasingState.FUTURE.toString(), user.getId()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(2)))
            .andExpect(jsonPath("$.content.[0].id").value(is(leasing1.getId().intValue())))
            .andExpect(jsonPath("$.content.[1].id").value(is(leasing2.getId().intValue())));
    }

    @Transactional
    public GardenField createGardenField() {
        GardenField gardenField = new GardenField();
        gardenField.setName("testGarden");
        gardenField.setSizeInM2(10.0);
        gardenField.setPricePerM2(3.0);
        gardenField.setLatitude(1.0);
        gardenField.setLongitude(2.0);
        gardenField.setCity("testCity");
        gardenField.setOwner(user);
        return gardenFieldRepository.save(gardenField);
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void updateStripeAccountVerification() throws Exception {
        String stripeVerificationKey = RandomUtil.generateStripeVerificationKey();
        user.setStripeVerificationKey(stripeVerificationKey);
        userRepository.saveAndFlush(user);

        UpdateStripeAccountVerifiedUserDTO updateStripeAccountVerifiedUserDTO = new UpdateStripeAccountVerifiedUserDTO(true, stripeVerificationKey);

        restUserMockMvc.perform(put("/api/v1/users/stripeVerification")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updateStripeAccountVerifiedUserDTO)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(is(Math.toIntExact(user.getId()))))
            .andExpect(jsonPath("$.stripeAccountVerified")
                .value(is(updateStripeAccountVerifiedUserDTO.getVerified())));
    }

    @Test
    @Transactional
    public void updateStripeAccountVerificationUnauthorized() throws Exception {
        String stripeVerificationKey = RandomUtil.generateStripeVerificationKey();
        user.setStripeVerificationKey(stripeVerificationKey);
        userRepository.saveAndFlush(user);

        UpdateStripeAccountVerifiedUserDTO updateStripeAccountVerifiedUserDTO = new UpdateStripeAccountVerifiedUserDTO(true, stripeVerificationKey);

        restUserMockMvc.perform(put("/api/v1/users/stripeVerification")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updateStripeAccountVerifiedUserDTO)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void updateStripeAccountVerificationForbidden() throws Exception {
        String stripeVerificationKey = RandomUtil.generateStripeVerificationKey();
        user.setStripeVerificationKey(stripeVerificationKey);
        userRepository.saveAndFlush(user);

        UpdateStripeAccountVerifiedUserDTO updateStripeAccountVerifiedUserDTO = new UpdateStripeAccountVerifiedUserDTO(true, "someString");

        restUserMockMvc.perform(put("/api/v1/users/stripeVerification")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updateStripeAccountVerifiedUserDTO)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isForbidden());
    }
}
