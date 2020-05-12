package com.gardle.web.rest;

import com.gardle.GardleApp;
import com.gardle.domain.GardenField;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.LeasingRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.GardenFieldService;
import com.gardle.service.LeasingService;
import com.gardle.service.dto.GardenFieldDTO;
import com.gardle.service.dto.SimpleUserDTO;
import com.gardle.service.mapper.SimpleUserMapper;
import com.gardle.web.rest.errors.ExceptionTranslator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GardleApp.class)
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GardenFieldSearchIT {

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
    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String DEFAULT_EMAIL = "johndoe@localhost";
    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String DEFAULT_LASTNAME = "doe";
    private static final String DESCRIPTION = "test description";
    private static final String DEFAULT_IBAN = "AT89370400440532013000";

    private static final String DEFAULT_PAYMENT_SESSION_ID = "cs_test_m5CBqcXSIJeKW7Ijb5vp8D9BrvDlJ6lRn25m6BdTZ0a1cLbIz5xVQ7bX";


    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private GardenFieldService gardenFieldService;

    @Autowired
    private LeasingService leasingService;

    @Autowired
    private GardenFieldRepository gardenFieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeasingRepository leasingRepository;

    @Autowired
    private SimpleUserMapper simpleUserMapper;

    private MockMvc restGardenFieldMockMvc;
    private SimpleUserDTO simpleUserDTO;

    @BeforeEach
    public void setup() {
        GardenFieldController gardenFieldController = new GardenFieldController(gardenFieldService, leasingService);

        this.restGardenFieldMockMvc = MockMvcBuilders.standaloneSetup(gardenFieldController)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .build();

        User user = new User();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        user.setStripeAccountVerified(true);
        User savedUser = userRepository.saveAndFlush(user);
        simpleUserDTO = simpleUserMapper.toDTO(savedUser);
    }

    @Test
    @WithMockUser(value = DEFAULT_LOGIN)
    public void searchGardenFieldForDescription() throws Exception {
        String name1 = "garden1";
        String name2 = "garden2";
        String name3 = "garden3";
        String desc1 = "Wenig Sonne, kleines Gartenfeld.";
        String desc2 = "Netter Garten in ruhiger Umgebung";
        String desc3 = "Cooler Garten mit viel Sonne";

        GardenFieldDTO gardenFieldDTO1 = createGardenFieldDTOWithoutOwner();
        GardenFieldDTO gardenFieldDTO2 = createGardenFieldDTOWithoutOwner();
        GardenFieldDTO gardenFieldDTO3 = createGardenFieldDTOWithoutOwner();

        gardenFieldDTO1.setOwner(simpleUserDTO);
        gardenFieldDTO1.setName(name1);
        gardenFieldDTO1.setDescription(desc1);

        gardenFieldDTO2.setOwner(simpleUserDTO);
        gardenFieldDTO2.setName(name2);
        gardenFieldDTO2.setDescription(desc2);

        gardenFieldDTO3.setOwner(simpleUserDTO);
        gardenFieldDTO3.setName(name3);
        gardenFieldDTO3.setDescription(desc3);

        gardenFieldService.createGardenField(gardenFieldDTO1);
        gardenFieldService.createGardenField(gardenFieldDTO2);
        gardenFieldService.createGardenField(gardenFieldDTO3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/filter?keywords=Sonne")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(2)))
            .andExpect(jsonPath("$.content.[0].name").value(is("garden1")))
            .andExpect(jsonPath("$.content.[1].name").value(is("garden3")));
    }

    @Test
    @WithMockUser(value = DEFAULT_LOGIN)
    public void testGardenFieldFilterLocation() throws Exception {
        String filterString = "latitude=" + LATITUDE + "&longitude=" + LONGITUDE + "&radiusInKM=50";
        String name1 = "gardenname1";
        String name2 = "gardenname2";

        GardenFieldDTO gardenFieldDTO1 = createGardenFieldDTOWithoutOwner();
        GardenFieldDTO gardenFieldDTO2 = createGardenFieldDTOWithoutOwner();
        GardenFieldDTO gardenFieldDTO3 = createGardenFieldDTOWithoutOwner();

        gardenFieldDTO1.setOwner(simpleUserDTO);
        gardenFieldDTO1.setName(name1);
        gardenFieldDTO1.setLatitude(LATITUDE);
        gardenFieldDTO1.setLongitude(LONGITUDE);

        gardenFieldDTO2.setOwner(simpleUserDTO);
        gardenFieldDTO2.setName(name2);
        gardenFieldDTO2.setLatitude(LATITUDE + 0.1);
        gardenFieldDTO2.setLongitude(LONGITUDE + 0.1);

        gardenFieldDTO3.setOwner(simpleUserDTO);
        //gardenfield 3 should not be in the radius 5, as gardenfield 1 and 2 are
        gardenFieldDTO3.setLatitude(LATITUDE + 123.23);
        gardenFieldDTO3.setLongitude(LONGITUDE + 13.876);

        gardenFieldService.createGardenField(gardenFieldDTO1);
        gardenFieldService.createGardenField(gardenFieldDTO2);
        gardenFieldService.createGardenField(gardenFieldDTO3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/filter?" + filterString)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(2)))
            .andExpect(jsonPath("$.content.[0].name").value(is(name1)))
            .andExpect(jsonPath("$.content.[1].name").value(is(name2)));
    }

    @Test
    @WithMockUser(value = DEFAULT_LOGIN)
    public void testGardenFieldFilterSize() throws Exception {
        String filterString = "sizeInM2LowerBound=3&sizeInM2UpperBound=9";
        String name1 = "gardenfield1";
        String name2 = "gardenfield2";
        String name3 = "gardenfield3";

        GardenFieldDTO gardenFieldDTO1 = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO1.setName(name1);
        gardenFieldDTO1.setSizeInM2(5.0);
        gardenFieldDTO1.setOwner(simpleUserDTO);

        GardenFieldDTO gardenFieldDTO2 = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO2.setName(name2);
        gardenFieldDTO2.setSizeInM2(8.9);
        gardenFieldDTO2.setOwner(simpleUserDTO);

        GardenFieldDTO gardenFieldDTO3 = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO3.setName(name3);
        gardenFieldDTO3.setSizeInM2(10.0);
        gardenFieldDTO3.setOwner(simpleUserDTO);

        gardenFieldService.createGardenField(gardenFieldDTO1);
        gardenFieldService.createGardenField(gardenFieldDTO2);
        gardenFieldService.createGardenField(gardenFieldDTO3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/filter?" + filterString)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.[0].name").value(is(name1)))
            .andExpect(jsonPath("$.content.[1].name").value(is(name2)))
            .andExpect(jsonPath("$.content.length()").value(is(2)));
    }

    @Test
    @WithMockUser(value = DEFAULT_LOGIN)
    public void testGardenFieldFilterWithInvalidCriteriaMissingRadius_shouldReturnValidationError() throws Exception {
        String filterString = "latitude=1.0&longitude=2.0";

        GardenFieldDTO gardenFieldDTO1 = createGardenFieldDTOWithoutOwner();
        GardenFieldDTO gardenFieldDTO2 = createGardenFieldDTOWithoutOwner();
        GardenFieldDTO gardenFieldDTO3 = createGardenFieldDTOWithoutOwner();

        gardenFieldDTO1.setOwner(simpleUserDTO);
        gardenFieldDTO2.setOwner(simpleUserDTO);
        gardenFieldDTO3.setOwner(simpleUserDTO);

        gardenFieldDTO1.setSizeInM2(5.0);
        gardenFieldDTO2.setSizeInM2(10.0);
        gardenFieldDTO3.setSizeInM2(8.9);

        gardenFieldService.createGardenField(gardenFieldDTO1);
        gardenFieldService.createGardenField(gardenFieldDTO2);
        gardenFieldService.createGardenField(gardenFieldDTO3);

        Exception exception = restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/filter?" + filterString)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andReturn().getResolvedException();
        assertThat(Objects.requireNonNull(exception).getMessage()).contains("filter").contains("invalid");
    }

    @Test
    @WithMockUser(value = DEFAULT_LOGIN)
    public void testLeasingTimeFromTofilter() throws Exception {
        String filterString = "leasingTimeFrom=2021-01-01T04:00:00.000Z&leasingTimeTo=2021-04-01T04:00:00.000Z";
        String name1 = "gardenname1";
        GardenFieldDTO gardenFieldDTO1 = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO1.setName(name1);
        GardenFieldDTO gardenFieldDTO2 = createGardenFieldDTOWithoutOwner();

        gardenFieldDTO1.setOwner(simpleUserDTO);
        gardenFieldDTO2.setOwner(simpleUserDTO);

        gardenFieldDTO1.setSizeInM2(5.0);
        gardenFieldDTO2.setSizeInM2(10.0);

        gardenFieldDTO1 = gardenFieldService.createGardenField(gardenFieldDTO1);
        gardenFieldDTO2 = gardenFieldService.createGardenField(gardenFieldDTO2);

        GardenField gardenField1 = gardenFieldRepository.findById(gardenFieldDTO1.getId()).get();
        GardenField gardenField2 = gardenFieldRepository.findById(gardenFieldDTO2.getId()).get();

        Leasing leasing1 = new Leasing()
            .from(Instant.parse("2020-01-01T04:00:00.000Z"))
            .to(Instant.parse("2020-10-01T04:00:50.000Z"))
            .status(LeasingStatus.RESERVED)
            .gardenField(gardenField1)
            .paymentSessionId(DEFAULT_PAYMENT_SESSION_ID)
            .user(userRepository.getOne(simpleUserDTO.getId()));
        Leasing leasing2 = new Leasing()
            .from(Instant.parse("2021-03-01T04:00:00.000Z"))
            .to(Instant.parse("2021-10-01T04:00:50.000Z"))
            .status(LeasingStatus.RESERVED)
            .gardenField(gardenField2)
            .paymentSessionId(DEFAULT_PAYMENT_SESSION_ID)
            .user(userRepository.getOne(simpleUserDTO.getId()));
        leasingRepository.save(leasing1);
        leasingRepository.save(leasing2);
        leasingRepository.flush();
        gardenField1.setLeasings(Collections.singletonList(leasing1));
        gardenField2.setLeasings(Collections.singletonList(leasing2));
        gardenFieldRepository.save(gardenField1);
        gardenFieldRepository.save(gardenField2);
        gardenFieldRepository.flush();

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/filter?" + filterString)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(1)))
            .andExpect(jsonPath("$.content.[0].name").value(is(name1)));
    }

    @Test
    @WithMockUser(value = DEFAULT_LOGIN)
    public void testBooleanFilters() throws Exception {
        String filterString = "water=true&electricity=true&roofed=false&high=false&glassHouse=false";
        GardenFieldDTO gardenFieldDTO1 = createGardenFieldDTO();
        GardenFieldDTO gardenFieldDTO2 = createGardenFieldDTO();
        GardenFieldDTO gardenFieldDTO3 = createGardenFieldDTO();

        gardenFieldDTO1.setWater(true);
        gardenFieldDTO1.setElectricity(true);
        gardenFieldDTO1.setRoofed(false);
        gardenFieldDTO1.setHigh(false);
        gardenFieldDTO1.setGlassHouse(false);
        gardenFieldDTO1.setName("gardenname1");

        gardenFieldService.createGardenField(gardenFieldDTO1);
        gardenFieldService.createGardenField(gardenFieldDTO2);
        gardenFieldService.createGardenField(gardenFieldDTO3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/filter?" + filterString)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(1)))
            .andExpect(jsonPath("$.content.[0].name").value(is("gardenname1")));
    }

    @Test
    @WithMockUser(value = DEFAULT_LOGIN)
    public void testGardenFieldCombinedFilter() throws Exception {
        String filterString = "maxPrice=300.0&roofed=true&latitude=1.0&longitude=2.0&radiusInKM=5";
        String name1 = "gardenname1";
        GardenFieldDTO gardenFieldDTO1 = createGardenFieldDTOWithoutOwner();
        GardenFieldDTO gardenFieldDTO2 = createGardenFieldDTOWithoutOwner();
        GardenFieldDTO gardenFieldDTO3 = createGardenFieldDTOWithoutOwner();

        gardenFieldDTO1.setOwner(simpleUserDTO);
        gardenFieldDTO1.setName(name1);
        gardenFieldDTO2.setOwner(simpleUserDTO);
        gardenFieldDTO3.setOwner(simpleUserDTO);

        gardenFieldDTO1.setPricePerM2(1.0);
        gardenFieldDTO1.setRoofed(true);
        gardenFieldDTO1.setSizeInM2(10.0);

        gardenFieldDTO2.setPricePerM2(1.5);
        gardenFieldDTO2.setRoofed(false);
        gardenFieldDTO2.setSizeInM2(10.0);

        gardenFieldDTO3.setPricePerM2(2.1);
        gardenFieldDTO3.setRoofed(true);
        gardenFieldDTO3.setSizeInM2(10.0);

        gardenFieldService.createGardenField(gardenFieldDTO1);
        gardenFieldService.createGardenField(gardenFieldDTO2);
        gardenFieldService.createGardenField(gardenFieldDTO3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/filter?" + filterString)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(1)))
            .andExpect(jsonPath("$.content.[0].name").value(is(name1)));
    }

    private GardenFieldDTO createGardenFieldDTOWithoutOwner() {
        GardenFieldDTO gardenFieldDTO = new GardenFieldDTO();
        gardenFieldDTO.setName(GARDEN_NAME);
        gardenFieldDTO.setSizeInM2(SIZE_IN_M2);
        gardenFieldDTO.setPricePerM2(PRICE_PER_M2);
        gardenFieldDTO.setLatitude(LATITUDE);
        gardenFieldDTO.setLongitude(LONGITUDE);
        gardenFieldDTO.setCity(CITY);
        gardenFieldDTO.setRoofed(ROOFED);
        gardenFieldDTO.setGlassHouse(GLASS_HOUSE);
        gardenFieldDTO.setHigh(HIGH);
        gardenFieldDTO.setWater(WATER);
        gardenFieldDTO.setElectricity(ELECTRICITY);
        gardenFieldDTO.setPhValue(PH_VALUE);
        gardenFieldDTO.setDescription(DESCRIPTION);
        return gardenFieldDTO;
    }

    private GardenFieldDTO createGardenFieldDTO() {
        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO.setOwner(simpleUserDTO);
        return gardenFieldDTO;
    }
}

