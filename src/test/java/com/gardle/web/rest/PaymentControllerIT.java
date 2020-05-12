package com.gardle.web.rest;

import com.gardle.GardleApp;
import com.gardle.domain.GardenField;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.LeasingRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.PaymentService;
import com.gardle.service.UserService;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import com.gardle.service.mapper.CreatorLeasingMapper;
import com.gardle.web.rest.errors.ExceptionTranslator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.gardle.web.rest.TestUtil.createFormattingConversionService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GardleApp.class)
public class PaymentControllerIT {

    private static final Instant DEFAULT_FROM = Instant.now().plus(20, ChronoUnit.DAYS); // has to be dynamic bc of validation
    private static final Instant DEFAULT_TO = Instant.now().plus(20, ChronoUnit.DAYS).plus(60, ChronoUnit.DAYS);
    private static final LeasingStatus DEFAULT_STATUS = LeasingStatus.OPEN;
    private static final String DEFAULT_IBAN = "AT89370400440532013000";
    private static final String GARDEN_NAME = "testGarden";
    private static final Double SIZE_IN_M2 = 10.0;
    private static final Double PRICE_PER_M2 = 3.0;
    private static final Boolean ROOFED = false;
    private static final Boolean GLASS_HOUSE = false;
    private static final Boolean HIGH = true;
    private static final Boolean WATER = true;
    private static final Boolean ELECTRICITY = false;
    private static final Double PH_VALUE = null;
    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String DEFAULT_EMAIL = "johndoe@localhost";
    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String DEFAULT_LASTNAME = "doe";

    @Autowired
    LeasingRepository leasingRepository;

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserService userService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    @Autowired
    private GardenFieldRepository gardenFieldRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc restPaymentMockMvc;

    @Autowired
    private CreatorLeasingMapper creatorLeasingMapper;

    private User user;
    private GardenField gardenField;
    private Leasing leasing;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PaymentController paymentController = new PaymentController(paymentService, userService);
        this.restPaymentMockMvc = MockMvcBuilders.standaloneSetup(paymentController)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }


    @BeforeEach
    public void initTest() {
        user = new User();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        user.setStripeAccountId("testId");
        userRepository.save(user);
        userRepository.flush();

        gardenField = new GardenField();
        gardenField.setName(GARDEN_NAME);
        gardenField.setSizeInM2(SIZE_IN_M2);
        gardenField.setPricePerM2(PRICE_PER_M2);
        gardenField.setLatitude(1.0);
        gardenField.setLongitude(1.0);
        gardenField.setCity("testcity");
        gardenField.setRoofed(ROOFED);
        gardenField.setGlassHouse(GLASS_HOUSE);
        gardenField.setHigh(HIGH);
        gardenField.setWater(WATER);
        gardenField.setElectricity(ELECTRICITY);
        gardenField.setPhValue(PH_VALUE);
        gardenField.setOwner(user);
        gardenFieldRepository.save(gardenField);
        gardenFieldRepository.flush();

        leasing = new Leasing()
            .from(DEFAULT_FROM)
            .to(DEFAULT_TO)
            .status(DEFAULT_STATUS)
            .gardenField(gardenField)
            .user(user).paymentSessionId("paymentSessionId");
        em.persist(leasing);
        em.flush();
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void testCreateCheckoutSession() throws Exception {
        leasingRepository.deleteAll();
        leasingRepository.flush();

        CreatorLeasingDTO creatorLeasingDTO = creatorLeasingMapper.toDto(leasing);

        restPaymentMockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/checkoutSession")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(creatorLeasingDTO)))
            .andExpect(status().isCreated());
    }

    @Test
    @Transactional
    public void testCreateCheckoutSessionShouldFailNotAuthorized() throws Exception {
        leasingRepository.deleteAll();
        leasingRepository.flush();

        CreatorLeasingDTO creatorLeasingDTO = creatorLeasingMapper.toDto(leasing);

        restPaymentMockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/checkoutSession")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(creatorLeasingDTO)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void testCreateCheckoutSessionInvalidCreatorDTOShouldFail() throws Exception {
        leasingRepository.deleteAll();
        leasingRepository.flush();

        CreatorLeasingDTO creatorLeasingDTO = creatorLeasingMapper.toDto(leasing);
        creatorLeasingDTO.setTo(Instant.ofEpochMilli(0L));
        creatorLeasingDTO.setFrom(creatorLeasingDTO.getTo().plus(2, ChronoUnit.DAYS));

        restPaymentMockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/checkoutSession")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(creatorLeasingDTO)))
            .andExpect(status().isBadRequest())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void testCreateCheckoutSessionInvalidCreatorDTOShouldFailInvalidDate() throws Exception {
        leasingRepository.deleteAll();
        leasingRepository.flush();

        CreatorLeasingDTO creatorLeasingDTO = creatorLeasingMapper.toDto(leasing);
        creatorLeasingDTO.setTo(Instant.now().plus(2, ChronoUnit.DAYS));
        creatorLeasingDTO.setFrom(Instant.now());

        restPaymentMockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/checkoutSession")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(creatorLeasingDTO)))
            .andExpect(status().isBadRequest())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void testCreateCheckoutSessionInvalidCreatorDTOShouldFailDateInPast() throws Exception {
        leasingRepository.deleteAll();
        leasingRepository.flush();

        CreatorLeasingDTO creatorLeasingDTO = creatorLeasingMapper.toDto(leasing);
        creatorLeasingDTO.setTo(Instant.now().minus(2, ChronoUnit.DAYS));
        creatorLeasingDTO.setFrom(Instant.now());

        restPaymentMockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/checkoutSession")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(creatorLeasingDTO)))
            .andExpect(status().isBadRequest())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void testCreateCheckoutSessionInvalidCreatorDTOShouldFailOverlaps() throws Exception {
        Leasing overlappingLesaing = new Leasing()
            .from(DEFAULT_FROM)
            .to(DEFAULT_TO.plus(10, ChronoUnit.DAYS))
            .status(DEFAULT_STATUS)
            .gardenField(gardenField)
            .user(user).paymentSessionId("paymentSessionId");
        em.persist(overlappingLesaing);
        em.flush();

        CreatorLeasingDTO creatorLeasingDTO = creatorLeasingMapper.toDto(leasing);

        restPaymentMockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/checkoutSession")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(creatorLeasingDTO)))
            .andExpect(status().isBadRequest())
            .andDo(MockMvcResultHandlers.print());
    }


    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void testGetAccountLinkUrlShouldFailWithInvalidAccount() throws Exception {
        restPaymentMockMvc.perform(get("/api/v1/payments/accountLinkUrl"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    public void testGetAccountLinkUrlWithUnauthorized() throws Exception {
        CreatorLeasingDTO creatorLeasingDTO = creatorLeasingMapper.toDto(leasing);
        creatorLeasingDTO.setUserId(-1L); //set to any invalid id

        restPaymentMockMvc.perform(get("/api/v1/payments/accountLinkUrl"))
            .andExpect(status().isUnauthorized());
    }

}
