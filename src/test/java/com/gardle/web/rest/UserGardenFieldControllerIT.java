package com.gardle.web.rest;

import com.gardle.GardleApp;
import com.gardle.domain.GardenField;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.LeasingState;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.LeasingRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.dto.SimpleUserDTO;
import com.gardle.service.mapper.SimpleUserMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GardleApp.class)
@ExtendWith(SpringExtension.class)
class UserGardenFieldControllerIT {

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
    private static final String DESCRIPTION = "test description";
    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String DEFAULT_EMAIL = "johndoe@localhost.com";
    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String DEFAULT_LASTNAME = "doe";
    private static final String DEFAULT_LOGIN2 = "testuser";
    private static final String DEFAULT_EMAIL2 = "testuser@localhost.com";
    private static final String DEFAULT_FIRSTNAME2 = "test";
    private static final String DEFAULT_LASTNAME2 = "user";
    private static final String DEFAULT_IBAN = "AT89370400440532013000";

    @Autowired
    private GardenFieldRepository gardenFieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private SimpleUserMapper simpleUserMapper;

    @Autowired
    private LeasingRepository leasingRepository;

    private MockMvc restGardenFieldMockMvc;
    private SimpleUserDTO simpleUserDTO;
    private User user;
    private User user2;

    private static final String DEFAULT_PAYMENT_SESSION_ID = "cs_test_m5CBqcXSIJeKW7Ijb5vp8D9BrvDlJ6lRn25m6BdTZ0a1cLbIz5xVQ7bX";

    @BeforeEach
    public void setup() {
        this.restGardenFieldMockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    @BeforeEach
    public void createUser() {
        user = new User();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        user = userRepository.save(user);
        simpleUserDTO = simpleUserMapper.toDTO(user);

        user2 = new User();
        user2.setLogin(DEFAULT_LOGIN2);
        user2.setPassword(RandomStringUtils.random(60));
        user2.setActivated(true);
        user2.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL2);
        user2.setFirstName(DEFAULT_FIRSTNAME2);
        user2.setLastName(DEFAULT_LASTNAME2);
        user2.setBankAccountIBAN(DEFAULT_IBAN);
        user2 = userRepository.saveAndFlush(user2);
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getAllUserGardenfields() throws Exception {
        createAndSaveGardenField(GARDEN_NAME, user);
        createAndSaveGardenField(GARDEN_NAME + "_2", user2);
        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/user")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content.[0].name").value(is(GARDEN_NAME)))
            .andExpect(jsonPath("$.content.[0].sizeInM2").value(is(SIZE_IN_M2)))
            .andExpect(jsonPath("$.content.[0].pricePerM2").value(is(PRICE_PER_M2)))
            .andExpect(jsonPath("$.content.[0].latitude").value(is(LATITUDE)))
            .andExpect(jsonPath("$.content.[0].longitude").value(is(LONGITUDE)))
            .andExpect(jsonPath("$.content.[0].city").value(is(CITY)))
            .andExpect(jsonPath("$.content.[0].roofed").value(is(ROOFED)))
            .andExpect(jsonPath("$.content.[0].glassHouse").value(is(GLASS_HOUSE)))
            .andExpect(jsonPath("$.content.[0].high").value(is(HIGH)))
            .andExpect(jsonPath("$.content.[0].water").value(is(WATER)))
            .andExpect(jsonPath("$.content.[0].electricity").value(is(ELECTRICITY)))
            .andExpect(jsonPath("$.content.[0].phValue").value(is(PH_VALUE)))
            .andExpect(jsonPath("$.content.[0].description").value(is(DESCRIPTION)))
            .andExpect(jsonPath("$.content.[0].owner.id").value(is(Math.toIntExact(simpleUserDTO.getId()))));
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getLeasingsForUser() throws Exception {
        GardenField gardenField1 = createAndSaveGardenField(GARDEN_NAME, user);
        GardenField gardenField2 = createAndSaveGardenField(GARDEN_NAME + "2", user2);
        Instant from1 = Instant.now();
        Instant to1 = from1.plus(100, ChronoUnit.DAYS);
        Instant from2 = Instant.now().plus(100, ChronoUnit.DAYS);
        Instant to2 = from2.plus(200, ChronoUnit.DAYS);
        Instant from3 = Instant.now().plus(100, ChronoUnit.DAYS);
        Instant to3 = from3.plus(200, ChronoUnit.DAYS);

        Leasing leasing1 = new Leasing();
        leasing1.setFrom(from1);
        leasing1.setTo(to1);
        leasing1.setGardenField(gardenField1);
        leasing1.setUser(user);
        leasing1.setStatus(LeasingStatus.OPEN);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);

        Leasing leasing2 = new Leasing();
        leasing2.setFrom(from2);
        leasing2.setTo(to2);
        leasing2.setGardenField(gardenField1);
        leasing2.setUser(user);
        leasing2.setStatus(LeasingStatus.RESERVED);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);

        Leasing leasing3 = new Leasing();
        leasing3.setFrom(from3);
        leasing3.setTo(to3);
        leasing3.setGardenField(gardenField2);
        leasing3.setUser(user2);
        leasing3.setStatus(LeasingStatus.RESERVED);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);

        leasingRepository.save(leasing1);
        leasingRepository.save(leasing2);
        leasingRepository.save(leasing3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/user/leasings")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content.[0].from").value(is(from1.toString())))
            .andExpect(jsonPath("$.content.[0].to").value(is(to1.toString())))
            .andExpect(jsonPath("$.content.[0].gardenField.name").value(is(GARDEN_NAME)))
            .andExpect(jsonPath("$.content.[1].gardenField.name").value(is(GARDEN_NAME)))
            .andExpect(jsonPath("$.content.[1].from").value(is(from2.toString())))
            .andExpect(jsonPath("$.content.[1].to").value(is(to2.toString())));
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getLeasingsForUserWithStateFilterOngoing() throws Exception {
        GardenField gardenField1 = createAndSaveGardenField(GARDEN_NAME, user);
        Instant from1 = Instant.now().minus(10, ChronoUnit.DAYS);
        Instant to1 = from1.plus(100, ChronoUnit.DAYS);
        Instant from2 = Instant.now().plus(100, ChronoUnit.DAYS);
        Instant to2 = from2.plus(200, ChronoUnit.DAYS);
        Instant from3 = Instant.now().minus(200, ChronoUnit.DAYS);
        Instant to3 = from3.minus(100, ChronoUnit.DAYS);

        Leasing leasing1 = new Leasing();
        leasing1.setFrom(from1);
        leasing1.setTo(to1);
        leasing1.setGardenField(gardenField1);
        leasing1.setUser(user);
        leasing1.setStatus(LeasingStatus.OPEN);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);

        Leasing leasing2 = new Leasing();
        leasing2.setFrom(from2);
        leasing2.setTo(to2);
        leasing2.setGardenField(gardenField1);
        leasing2.setUser(user);
        leasing2.setStatus(LeasingStatus.RESERVED);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);

        Leasing leasing3 = new Leasing();
        leasing3.setFrom(from3);
        leasing3.setTo(to3);
        leasing3.setGardenField(gardenField1);
        leasing3.setUser(user);
        leasing3.setStatus(LeasingStatus.RESERVED);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);

        leasingRepository.save(leasing1);
        leasingRepository.save(leasing2);
        leasingRepository.save(leasing3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/user/leasings?state=" + LeasingState.ONGOING.toString())
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content.[0].from").value(is(from1.toString())))
            .andExpect(jsonPath("$.content.[0].to").value(is(to1.toString())))
            .andExpect(jsonPath("$.content.[0].gardenField.name").value(is(GARDEN_NAME)));
    }

    private GardenField createAndSaveGardenField(String gardenName, User user) {
        GardenField gardenField = new GardenField();
        gardenField.setName(gardenName);
        gardenField.setDescription(DESCRIPTION);
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
        gardenField.setOwner(user);
        return gardenFieldRepository.saveAndFlush(gardenField);
    }
}
