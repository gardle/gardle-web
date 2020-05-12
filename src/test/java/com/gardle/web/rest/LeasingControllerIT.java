package com.gardle.web.rest;

import com.gardle.GardleApp;
import com.gardle.domain.GardenField;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.LeasingRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.LeasingQueryService;
import com.gardle.service.LeasingService;
import com.gardle.service.PaymentService;
import com.gardle.service.dto.leasing.LeasingDTO;
import com.gardle.service.dto.leasing.UpdatingLeasingDTO;
import com.gardle.service.mapper.CreatorLeasingMapper;
import com.gardle.service.mapper.LeasingMapper;
import com.gardle.service.mapper.UpdatingLeasingMapper;
import com.gardle.web.rest.errors.ExceptionTranslator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.gardle.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GardleApp.class)
public class LeasingControllerIT {

    private final Logger log = LoggerFactory.getLogger(LeasingControllerIT.class);

    private static final Instant DEFAULT_FROM = Instant.now().plus(100, ChronoUnit.DAYS);
    private static final Instant UPDATED_FROM = DEFAULT_FROM.plus(2, ChronoUnit.DAYS);
    private static final Instant DEFAULT_TO = DEFAULT_FROM.plus(180, ChronoUnit.DAYS); // + 6 months
    private static final Instant UPDATED_TO = DEFAULT_TO.plus(180, ChronoUnit.DAYS); // + 6 months
    private static final LeasingStatus DEFAULT_STATUS = LeasingStatus.OPEN;
    private static final LeasingStatus UPDATED_STATUS = LeasingStatus.RESERVED;
    private static final String GARDEN_NAME = "testGarden";
    private static final Double SIZE_IN_M2 = 10.0;
    private static final Double PRICE_PER_M2 = 3.0;
    private static final Double LATITUDE = 1.0;
    private static final Double LONGITUDE = 2.0;
    private static final String CITY = "testCity";
    private static final Boolean ROOFED = false;
    private static final Boolean GLASS_HOUSE = false;
    private static final Boolean HIGH = true;
    private static final Boolean WATER = true;
    private static final Boolean ELECTRICITY = false;
    private static final Double PH_VALUE = null;
    private static final String REQUESTER_LOGIN = "johndoe";
    private static final String OWNER_LOGIN = "owner_johndoe";
    private static final String OTHER_USER_LOGIN = "otherjohndoe";
    private static final String DEFAULT_EMAIL = "johndoe@localhost";
    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String DEFAULT_LASTNAME = "doe";
    private static final String DEFAULT_IBAN = "AT89370400440532013000";

    @Autowired
    private LeasingRepository leasingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GardenFieldRepository gardenFieldRepository;

    @Autowired
    private LeasingMapper leasingMapper;

    @Autowired
    private UpdatingLeasingMapper updatingLeasingMapper;

    @Autowired
    private LeasingService leasingService;

    @Autowired
    private LeasingQueryService leasingQueryService;

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
    private PaymentService paymentService;

    @Autowired
    private CreatorLeasingMapper creatorLeasingMapper;

    private MockMvc restLeasingMockMvc;

    private GardenField gardenField;
    private Leasing leasing;
    private User requester;
    private User owner;
    private User otherUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        final LeasingController leasingController = new LeasingController(leasingService, leasingQueryService);
        this.restLeasingMockMvc = MockMvcBuilders.standaloneSetup(leasingController)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    @BeforeEach
    public void initTest() {
        createRequester();
        createOwner();
        createOtherUser();

        gardenField = new GardenField();
        gardenField.setName(GARDEN_NAME);
        gardenField.setSizeInM2(SIZE_IN_M2);
        gardenField.setPricePerM2(PRICE_PER_M2);
        gardenField.setLatitude(LATITUDE);
        gardenField.setLongitude(LONGITUDE);
        gardenField.setCity(CITY);
        gardenField.setRoofed(ROOFED);
        gardenField.setGlassHouse(GLASS_HOUSE);
        gardenField.setHigh(HIGH);
        gardenField.setWater(WATER);
        gardenField.setElectricity(ELECTRICITY);
        gardenField.setPhValue(PH_VALUE);
        gardenField.setOwner(owner);
        gardenFieldRepository.saveAndFlush(gardenField);

        leasing = new Leasing()
            .from(DEFAULT_FROM)
            .to(DEFAULT_TO)
            .status(DEFAULT_STATUS)
            .gardenField(gardenField)
            .paymentSessionId("test")
            .user(requester);
    }

    private String getPaymentSessionId() {
        return paymentService.createCheckoutSession(creatorLeasingMapper.toDto(leasing)).getId();
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void updateNonExistingLeasing() throws Exception {
        int databaseSizeBeforeUpdate = leasingRepository.findAll().size();

        // Create the Leasing
        UpdatingLeasingDTO leasingDTO = updatingLeasingMapper.toDto(leasing);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLeasingMockMvc.perform(put("/api/v1/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(leasingDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Leasing in the database
        List<Leasing> leasingList = leasingRepository.findAll();
        assertThat(leasingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void updateLeasingInNotAllowedPeriodByRequester() throws Exception {
        leasing.setFrom(Instant.now().plus(13, ChronoUnit.DAYS)); //depends on UPDATE_DAY_RANGE in leasingService
        leasing.setTo(Instant.now().plus(200, ChronoUnit.DAYS));
        prepareUpdateTest();

        UpdatingLeasingDTO leasingDTO = updatingLeasingMapper.toDto(leasing);
        leasingDTO.setStatus(LeasingStatus.CANCELLED);

        restLeasingMockMvc.perform(put("/api/v1/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(leasingDTO)))
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    @WithMockUser(OWNER_LOGIN)
    public void updateLeasingInNotAllowedPeriodByOwner() throws Exception {
        leasing.setFrom(Instant.now().plus(13, ChronoUnit.DAYS)); //depends on UPDATE_DAY_RANGE in leasingService
        leasing.setTo(Instant.now().plus(200, ChronoUnit.DAYS));
        prepareUpdateTest();

        UpdatingLeasingDTO leasingDTO = updatingLeasingMapper.toDto(leasing);
        leasingDTO.setStatus(LeasingStatus.RESERVED);

        restLeasingMockMvc.perform(put("/api/v1/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(leasingDTO)))
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    @WithMockUser(OWNER_LOGIN)
    public void updateLeasingNotAllowedFromReservedToRejectedByOwner() throws Exception {
        leasing.setStatus(LeasingStatus.RESERVED);
        prepareUpdateTest();

        UpdatingLeasingDTO leasingDTO = updatingLeasingMapper.toDto(leasing);
        leasingDTO.setStatus(LeasingStatus.REJECTED);

        restLeasingMockMvc.perform(put("/api/v1/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(leasingDTO)))
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    @WithMockUser(OWNER_LOGIN)
    public void updateLeasingNotAllowedFromCancelledToReservedByOwner() throws Exception {
        leasing.setStatus(LeasingStatus.CANCELLED);
        prepareUpdateTest();

        UpdatingLeasingDTO leasingDTO = updatingLeasingMapper.toDto(leasing);
        leasingDTO.setStatus(LeasingStatus.RESERVED);

        restLeasingMockMvc.perform(put("/api/v1/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(leasingDTO)))
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void updateLeasingNotAllowedFromOpenToReservedByRequester() throws Exception {
        prepareUpdateTest();

        UpdatingLeasingDTO leasingDTO = updatingLeasingMapper.toDto(leasing);
        leasingDTO.setStatus(LeasingStatus.RESERVED);

        restLeasingMockMvc.perform(put("/api/v1/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(leasingDTO)))
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    @WithMockUser(OWNER_LOGIN)
    public void updateLeasingFromOpenToRejectedInUpdateDayRangeByOwner() throws Exception {
        leasing.setFrom(Instant.now().plus(1, ChronoUnit.DAYS)); //depends on UPDATE_DAY_RANGE in leasingService
        leasing.setTo(Instant.now().plus(200, ChronoUnit.DAYS));
        prepareUpdateTest();

        UpdatingLeasingDTO leasingDTO = updatingLeasingMapper.toDto(leasing);
        leasingDTO.setStatus(LeasingStatus.REJECTED);

        restLeasingMockMvc.perform(put("/api/v1/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(leasingDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(is(LeasingStatus.REJECTED.toString())));
    }

    @Test
    @Transactional
    @WithMockUser(OWNER_LOGIN)
    public void updateLeasingFromOpenToRejectedOutOfUpdateRangeByOwner() throws Exception {
        prepareUpdateTest();

        UpdatingLeasingDTO leasingDTO = updatingLeasingMapper.toDto(leasing);
        leasingDTO.setStatus(LeasingStatus.REJECTED);

        restLeasingMockMvc.perform(put("/api/v1/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(leasingDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(is(LeasingStatus.REJECTED.toString())));
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void updateLeasingFromOpenToCancelledByRequester() throws Exception {
        prepareUpdateTest();

        UpdatingLeasingDTO leasingDTO = updatingLeasingMapper.toDto(leasing);
        leasingDTO.setStatus(LeasingStatus.CANCELLED);

        restLeasingMockMvc.perform(put("/api/v1/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(leasingDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(is(LeasingStatus.CANCELLED.toString())));
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getAllLeasings() throws Exception {
        // Initialize the database
        leasing.setPaymentSessionId(getPaymentSessionId());
        leasingRepository.saveAndFlush(leasing);

        // Get all the leasingList
        restLeasingMockMvc.perform(get("/api/v1/leasings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.[0].id").value(is(leasing.getId().intValue())))
            .andExpect(jsonPath("$.content.[0].from").value(is(DEFAULT_FROM.toString())))
            .andExpect(jsonPath("$.content.[0].to").value(is(DEFAULT_TO.toString())))
            .andExpect(jsonPath("$.content.[0].status").value(is(DEFAULT_STATUS.toString())));
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getLeasing() throws Exception {
        // Initialize the database
        leasing.setPaymentSessionId(getPaymentSessionId());
        leasingRepository.saveAndFlush(leasing);

        // Get the leasing
        restLeasingMockMvc.perform(get("/api/v1/leasings/{id}", leasing.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(leasing.getId().intValue()))
            .andExpect(jsonPath("$.from").value(DEFAULT_FROM.toString()))
            .andExpect(jsonPath("$.to").value(DEFAULT_TO.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getAllLeasingsByFromIsEqualToSomething() throws Exception {
        // Initialize the database
        leasingRepository.saveAndFlush(leasing);

        // Get all the leasingList where from equals to DEFAULT_FROM
        defaultLeasingShouldBeFound("from.equals=" + DEFAULT_FROM);

        // Get all the leasingList where from equals to UPDATED_FROM
        defaultLeasingShouldNotBeFound("from.equals=" + UPDATED_FROM);
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getAllLeasingsByFromIsInShouldWork() throws Exception {
        // Initialize the database
        leasingRepository.saveAndFlush(leasing);

        // Get all the leasingList where from in DEFAULT_FROM or UPDATED_FROM
        defaultLeasingShouldBeFound("from.in=" + DEFAULT_FROM + "," + UPDATED_FROM);

        // Get all the leasingList where from equals to UPDATED_FROM
        defaultLeasingShouldNotBeFound("from.in=" + UPDATED_FROM);
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getAllLeasingsByFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        leasingRepository.saveAndFlush(leasing);

        // Get all the leasingList where from is not null
        defaultLeasingShouldBeFound("from.specified=true");

        // Get all the leasingList where from is null
        defaultLeasingShouldNotBeFound("from.specified=false");
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getAllLeasingsByToIsEqualToSomething() throws Exception {
        // Initialize the database
        leasingRepository.saveAndFlush(leasing);

        // Get all the leasingList where to equals to DEFAULT_TO
        defaultLeasingShouldBeFound("to.equals=" + DEFAULT_TO);

        // Get all the leasingList where to equals to UPDATED_TO
        defaultLeasingShouldNotBeFound("to.equals=" + UPDATED_TO);
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getAllLeasingsByToIsInShouldWork() throws Exception {
        // Initialize the database
        leasingRepository.saveAndFlush(leasing);

        // Get all the leasingList where to in DEFAULT_TO or UPDATED_TO
        defaultLeasingShouldBeFound("to.in=" + DEFAULT_TO + "," + UPDATED_TO);

        // Get all the leasingList where to equals to UPDATED_TO
        defaultLeasingShouldNotBeFound("to.in=" + UPDATED_TO);
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getAllLeasingsByToIsNullOrNotNull() throws Exception {
        // Initialize the database
        leasingRepository.saveAndFlush(leasing);

        // Get all the leasingList where to is not null
        defaultLeasingShouldBeFound("to.specified=true");

        // Get all the leasingList where to is null
        defaultLeasingShouldNotBeFound("to.specified=false");
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getAllLeasingsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        leasingRepository.saveAndFlush(leasing);

        // Get all the leasingList where status equals to DEFAULT_STATUS
        defaultLeasingShouldBeFound("status.equals=" + DEFAULT_STATUS);

        // Get all the leasingList where status equals to UPDATED_STATUS
        defaultLeasingShouldNotBeFound("status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getAllLeasingsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        leasingRepository.saveAndFlush(leasing);

        // Get all the leasingList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultLeasingShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);

        // Get all the leasingList where status equals to UPDATED_STATUS
        defaultLeasingShouldNotBeFound("status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getAllLeasingsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        leasingRepository.saveAndFlush(leasing);

        // Get all the leasingList where status is not null
        defaultLeasingShouldBeFound("status.specified=true");

        // Get all the leasingList where status is null
        defaultLeasingShouldNotBeFound("status.specified=false");
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultLeasingShouldBeFound(String filter) throws Exception {
        restLeasingMockMvc.perform(get("/api/v1/leasings?sort=id,desc&" + filter))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.[0].id").value(is(leasing.getId().intValue())))
            .andExpect(jsonPath("$.content.[0].from").value(is(DEFAULT_FROM.toString())))
            .andExpect(jsonPath("$.content.[0].to").value(is(DEFAULT_TO.toString())))
            .andExpect(jsonPath("$.content.[0].status").value(is(DEFAULT_STATUS.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultLeasingShouldNotBeFound(String filter) throws Exception {
        restLeasingMockMvc.perform(get("/api/v1/leasings?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content").isEmpty());
    }


    @Test
    @Transactional
    @WithMockUser(REQUESTER_LOGIN)
    public void getNonExistingLeasing() throws Exception {
        // Get the leasing
        restLeasingMockMvc.perform(get("/api/v1/leasings/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void equalsVerifier() {
        Leasing leasing1 = new Leasing();
        leasing1.setId(1L);

        Leasing leasing2 = new Leasing();
        leasing2.setId(leasing1.getId());

        assertThat(leasing1).isEqualTo(leasing2);
        leasing2.setId(2L);

        assertThat(leasing1).isNotEqualTo(leasing2);
        leasing1.setId(null);
        assertThat(leasing1).isNotEqualTo(leasing2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() {
        LeasingDTO leasingDTO1 = new LeasingDTO();
        leasingDTO1.setId(1L);
        LeasingDTO leasingDTO2 = new LeasingDTO();
        assertThat(leasingDTO1).isNotEqualTo(leasingDTO2);
        leasingDTO2.setId(leasingDTO1.getId());
        assertThat(leasingDTO1).isEqualTo(leasingDTO2);
        leasingDTO2.setId(2L);
        assertThat(leasingDTO1).isNotEqualTo(leasingDTO2);
        leasingDTO1.setId(null);
        assertThat(leasingDTO1).isNotEqualTo(leasingDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(leasingMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(leasingMapper.fromId(null)).isNull();
    }

    @Test
    @Transactional
    @WithMockUser(OWNER_LOGIN)
    public void getLeasingDateRanges() throws Exception {
        Leasing leasing1 = new Leasing();
        Leasing leasing2 = new Leasing();
        Leasing leasing3 = new Leasing();

        leasing1.setGardenField(gardenField);
        leasing1.setUser(requester);
        leasing2.setGardenField(gardenField);
        leasing2.setUser(requester);
        leasing3.setGardenField(gardenField);
        leasing3.setUser(requester);

        leasing1.setStatus(LeasingStatus.RESERVED);
        leasing2.setStatus(LeasingStatus.RESERVED);
        leasing3.setStatus(LeasingStatus.REJECTED);

        Instant from1 = LocalDateTime.of(2019, Month.MARCH, 1, 0, 0).atZone(ZoneId.of("Europe/Vienna")).toInstant();
        Instant to1 = LocalDateTime.of(2019, Month.JUNE, 15, 23, 59).atZone(ZoneId.of("Europe/Vienna")).toInstant();
        Instant from2 = LocalDateTime.of(2019, Month.JUNE, 16, 0, 0).atZone(ZoneId.of("Europe/Vienna")).toInstant();
        Instant to2 = LocalDateTime.of(2019, Month.SEPTEMBER, 23, 23, 59).atZone(ZoneId.of("Europe/Vienna")).toInstant();
        Instant from3 = LocalDateTime.of(2019, Month.MAY, 16, 0, 0).atZone(ZoneId.of("Europe/Vienna")).toInstant();
        Instant to3 = LocalDateTime.of(2019, Month.AUGUST, 23, 23, 59).atZone(ZoneId.of("Europe/Vienna")).toInstant();

        leasing1.setFrom(from1);
        leasing1.setTo(to1);
        leasing1.setPaymentSessionId(getPaymentSessionId());
        leasing2.setFrom(from2);
        leasing2.setTo(to2);
        leasing2.setPaymentSessionId(getPaymentSessionId());
        leasing3.setFrom(from3);
        leasing3.setTo(to3);
        leasing3.setPaymentSessionId(getPaymentSessionId());

        leasingRepository.save(leasing1);
        leasingRepository.save(leasing2);
        leasingRepository.save(leasing3);

        assertThat(leasingRepository.findAll().size()).isEqualTo(3);

        restLeasingMockMvc.perform(get("/api/v1/leasings/{gardenFieldId}/" +
                "leasedDateRanges?from=" + from1.minus(1, ChronoUnit.DAYS) +
                "&to=" + to2.plus(1, ChronoUnit.DAYS)
            , leasing.getGardenField().getId()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.length()").value(is(2)))
            .andExpect(jsonPath("$.[0].from").value(is(from1.toString())))
            .andExpect(jsonPath("$.[0].to").value(is(to1.toString())))
            .andExpect(jsonPath("$.[1].from").value(is(from2.toString())))
            .andExpect(jsonPath("$.[1].to").value(is(to2.toString())));
    }

    private void createRequester() {
        requester = new User();
        requester.setLogin(REQUESTER_LOGIN);
        requester.setPassword(RandomStringUtils.random(60));
        requester.setActivated(true);
        requester.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        requester.setFirstName(DEFAULT_FIRSTNAME);
        requester.setLastName(DEFAULT_LASTNAME);
        requester.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(requester);
    }

    private void createOwner() {
        owner = new User();
        owner.setLogin(OWNER_LOGIN);
        owner.setPassword(RandomStringUtils.random(60));
        owner.setActivated(true);
        owner.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        owner.setFirstName("owner");
        owner.setLastName("owner");
        owner.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(owner);
    }

    private void createOtherUser() {
        otherUser = new User();
        otherUser.setLogin(OTHER_USER_LOGIN);
        otherUser.setPassword(RandomStringUtils.random(60));
        otherUser.setActivated(true);
        otherUser.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        otherUser.setFirstName("other");
        otherUser.setLastName("user");
        otherUser.setBankAccountIBAN(DEFAULT_IBAN);
        userRepository.saveAndFlush(otherUser);
    }

    private void prepareUpdateTest() {
        leasing.setPaymentSessionId(getPaymentSessionId());
        leasing = leasingRepository.saveAndFlush(leasing);
    }
}
