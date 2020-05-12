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
import com.gardle.service.GardenFieldService;
import com.gardle.service.dto.GardenFieldDTO;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GardleApp.class)
@ExtendWith(SpringExtension.class)
public class GardenFieldControllerIT {

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
    private static final String DEFAULT_PAYMENT_SESSION_ID = "cs_test_m5CBqcXSIJeKW7Ijb5vp8D9BrvDlJ6lRn25m6BdTZ0a1cLbIz5xVQ7bX";
    private static final String DEFAULT_IBAN = "AT89370400440532013000";


    @Autowired
    private GardenFieldService gardenFieldService;

    @Autowired
    private GardenFieldRepository gardenFieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeasingRepository leasingRepository;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private SimpleUserMapper simpleUserMapper;
    private MockMvc restGardenFieldMockMvc;
    private SimpleUserDTO simpleUserDTO;
    private User user;

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
        user.setStripeAccountVerified(true);
        user = userRepository.saveAndFlush(user);
        simpleUserDTO = simpleUserMapper.toDTO(user);
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createGardenField() throws Exception {
        int databaseSizeBeforeCreate = gardenFieldRepository.findAll().size();
        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO.setOwner(simpleUserDTO);

        restGardenFieldMockMvc.perform(post("/api/v1/gardenfields")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gardenFieldDTO)))
            .andExpect(status().isCreated());

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields).hasSize(databaseSizeBeforeCreate + 1);
        GardenField testGardenField = gardenFields.get(gardenFields.size() - 1);
        assertThat(testGardenField).isNotNull();
        assertThat(testGardenField.getName()).isEqualTo(GARDEN_NAME);
        assertThat(testGardenField.getSizeInM2()).isEqualTo(SIZE_IN_M2);
        assertThat(testGardenField.getPricePerM2()).isEqualTo(PRICE_PER_M2);
        assertThat(testGardenField.getLatitude()).isEqualTo(LATITUDE);
        assertThat(testGardenField.getLongitude()).isEqualTo(LONGITUDE);
        assertThat(testGardenField.getCity()).isEqualTo(CITY);
        assertThat(testGardenField.getRoofed()).isEqualTo(ROOFED);
        assertThat(testGardenField.getGlassHouse()).isEqualTo(GLASS_HOUSE);
        assertThat(testGardenField.getHigh()).isEqualTo(HIGH);
        assertThat(testGardenField.getElectricity()).isEqualTo(ELECTRICITY);
        assertThat(testGardenField.getPhValue()).isEqualTo(PH_VALUE);
        assertThat(testGardenField.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(testGardenField.getOwner()).isNotNull();
        assertThat(testGardenField.getOwner().getId()).isEqualTo(simpleUserDTO.getId());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createGardenFieldUserNotStripeVerifiedShouldThrowException() throws Exception {
        int databaseSizeBeforeCreate = gardenFieldRepository.findAll().size();
        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        user.setStripeAccountVerified(false);
        userRepository.saveAndFlush(user);
        gardenFieldDTO.setOwner(simpleUserDTO);

        assertThat(user.getStripeAccountVerified()).isFalse();
        restGardenFieldMockMvc.perform(post("/api/v1/gardenfields")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gardenFieldDTO)))
            .andExpect(status().isForbidden());

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createGardenFieldOwnerIsNull() throws Exception {
        int databaseSizeBeforeCreate = gardenFieldRepository.findAll().size();

        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO.setOwner(null);

        restGardenFieldMockMvc.perform(post("/api/v1/gardenfields")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gardenFieldDTO)))
            .andExpect(status().isCreated());

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields).hasSize(databaseSizeBeforeCreate + 1);
        GardenField testGardenField = gardenFields.get(gardenFields.size() - 1);
        assertThat(testGardenField).isNotNull();
        assertThat(testGardenField.getName()).isEqualTo(GARDEN_NAME);
        assertThat(testGardenField.getSizeInM2()).isEqualTo(SIZE_IN_M2);
        assertThat(testGardenField.getPricePerM2()).isEqualTo(PRICE_PER_M2);
        assertThat(testGardenField.getLatitude()).isEqualTo(LATITUDE);
        assertThat(testGardenField.getLongitude()).isEqualTo(LONGITUDE);
        assertThat(testGardenField.getCity()).isEqualTo(CITY);
        assertThat(testGardenField.getRoofed()).isEqualTo(ROOFED);
        assertThat(testGardenField.getGlassHouse()).isEqualTo(GLASS_HOUSE);
        assertThat(testGardenField.getHigh()).isEqualTo(HIGH);
        assertThat(testGardenField.getElectricity()).isEqualTo(ELECTRICITY);
        assertThat(testGardenField.getPhValue()).isEqualTo(PH_VALUE);
        assertThat(testGardenField.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(testGardenField.getOwner()).isNotNull();
        assertThat(testGardenField.getOwner().getId()).isEqualTo(simpleUserDTO.getId());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createGardenFieldOwnerIdIsNull() throws Exception {
        int databaseSizeBeforeCreate = gardenFieldRepository.findAll().size();

        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO.setOwner(new SimpleUserDTO());

        restGardenFieldMockMvc.perform(post("/api/v1/gardenfields")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gardenFieldDTO)))
            .andExpect(status().isBadRequest());

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createGardenFieldWithOtherOwner() throws Exception {
        int databaseSizeBeforeCreate = gardenFieldRepository.findAll().size();

        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        SimpleUserDTO otherUser = new SimpleUserDTO();
        otherUser.setId(simpleUserDTO.getId() + 1);
        otherUser.setFirstName("Hans");
        otherUser.setLastName("Wurst");
        otherUser.setEmail("test@localhost.com");
        otherUser.setLogin("hansi");
        gardenFieldDTO.setOwner(otherUser);

        restGardenFieldMockMvc.perform(post("/api/v1/gardenfields")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gardenFieldDTO)))
            .andExpect(status().isUnauthorized());

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createGardenFieldWithoutNecessaryFields() throws Exception {
        int databaseSizeBeforeCreate = gardenFieldRepository.findAll().size();
        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO.setName(null);
        gardenFieldDTO.setPricePerM2(null);
        gardenFieldDTO.setSizeInM2(null);

        restGardenFieldMockMvc.perform(post("/api/v1/gardenfields")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gardenFieldDTO)))
            .andExpect(status().isBadRequest());

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createGardenFieldWithSetId() throws Exception {
        int databaseSizeBeforeCreate = gardenFieldRepository.findAll().size();
        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO.setId(100L);

        restGardenFieldMockMvc.perform(post("/api/v1/gardenfields")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gardenFieldDTO)))
            .andExpect(status().isBadRequest());

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void updateGardenFieldName() throws Exception {
        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO = gardenFieldService.createGardenField(gardenFieldDTO);

        int databaseSizeAfterCreate = gardenFieldRepository.findAll().size();

        gardenFieldDTO.setName("updatedName");

        restGardenFieldMockMvc.perform(put("/api/v1/gardenfields")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gardenFieldDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(is("updatedName")));

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields.size()).isEqualTo(databaseSizeAfterCreate);
        assertThat(gardenFields.get(0).getName()).isEqualTo("updatedName");
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void updateGardenFieldOwner() throws Exception {
        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO = gardenFieldService.createGardenField(gardenFieldDTO);
        gardenFieldDTO.setId(gardenFieldDTO.getId());

        int databaseSizeAfterCreate = gardenFieldRepository.findAll().size();

        User user = new User();
        user.setLogin(DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5));
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        SimpleUserDTO newSimpleUserDTO = simpleUserMapper.toDTO(userRepository.saveAndFlush(user));
        gardenFieldDTO.setOwner(newSimpleUserDTO);

        restGardenFieldMockMvc.perform(put("/api/v1/gardenfields")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gardenFieldDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.owner.id").value(is(Math.toIntExact(newSimpleUserDTO.getId()))));

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields.size()).isEqualTo(databaseSizeAfterCreate);
        assertThat(gardenFields.get(0).getOwner().getId()).isEqualTo(Math.toIntExact(newSimpleUserDTO.getId()));
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void updateGardenFieldWithoutNecessaryFields() throws Exception {
        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO = gardenFieldService.createGardenField(gardenFieldDTO);
        gardenFieldDTO.setSizeInM2(null);

        int databaseSizeAfterCreate = gardenFieldRepository.findAll().size();

        restGardenFieldMockMvc.perform(put("/api/v1/gardenfields")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(gardenFieldDTO)))
            .andExpect(status().isBadRequest());

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields.size()).isEqualTo(databaseSizeAfterCreate);
        assertThat(gardenFields.get(0).getSizeInM2()).isEqualTo(SIZE_IN_M2);
    }

    @Test
    @Transactional
    public void getAllGardenFields() throws Exception {
        createAndSaveGardenField(GARDEN_NAME);
        createAndSaveGardenField(GARDEN_NAME + "_2");
        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
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
            .andExpect(jsonPath("$.content.[0].owner.id").value(is(Math.toIntExact(simpleUserDTO.getId()))))
            .andExpect(jsonPath("$.content.[1].name").value(is(GARDEN_NAME + "_2")))
            .andExpect(jsonPath("$.content.[1].sizeInM2").value(is(SIZE_IN_M2)))
            .andExpect(jsonPath("$.content.[1].pricePerM2").value(is(PRICE_PER_M2)))
            .andExpect(jsonPath("$.content.[1].latitude").value(is(LATITUDE)))
            .andExpect(jsonPath("$.content.[1].longitude").value(is(LONGITUDE)))
            .andExpect(jsonPath("$.content.[1].city").value(is(CITY)))
            .andExpect(jsonPath("$.content.[1].roofed").value(is(ROOFED)))
            .andExpect(jsonPath("$.content.[1].glassHouse").value(is(GLASS_HOUSE)))
            .andExpect(jsonPath("$.content.[1].high").value(is(HIGH)))
            .andExpect(jsonPath("$.content.[1].water").value(is(WATER)))
            .andExpect(jsonPath("$.content.[1].electricity").value(is(ELECTRICITY)))
            .andExpect(jsonPath("$.content.[1].phValue").value(is(PH_VALUE)))
            .andExpect(jsonPath("$.content.[1].description").value(is(DESCRIPTION)))
            .andExpect(jsonPath("$.content.[1].owner.id").value(is(Math.toIntExact(simpleUserDTO.getId()))));
    }

    @Test
    @Transactional
    public void findOneGardenField_Successful() throws Exception {
        GardenField gardenField = createAndSaveGardenField(GARDEN_NAME);
        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/{id}", gardenField.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.name").value(is(GARDEN_NAME)))
            .andExpect(jsonPath("$.sizeInM2").value(is(SIZE_IN_M2)))
            .andExpect(jsonPath("$.pricePerM2").value(is(PRICE_PER_M2)))
            .andExpect(jsonPath("$.latitude").value(is(LATITUDE)))
            .andExpect(jsonPath("$.longitude").value(is(LONGITUDE)))
            .andExpect(jsonPath("$.city").value(is(CITY)))
            .andExpect(jsonPath("$.roofed").value(is(ROOFED)))
            .andExpect(jsonPath("$.glassHouse").value(is(GLASS_HOUSE)))
            .andExpect(jsonPath("$.high").value(is(HIGH)))
            .andExpect(jsonPath("$.water").value(is(WATER)))
            .andExpect(jsonPath("$.electricity").value(is(ELECTRICITY)))
            .andExpect(jsonPath("$.phValue").value(is(PH_VALUE)))
            .andExpect(jsonPath("$.owner.id").value(is(Math.toIntExact(simpleUserDTO.getId()))));
    }

    @Test
    @Transactional
    public void findOneGardenField_NotFound() throws Exception {
        GardenField gardenField = createAndSaveGardenField(GARDEN_NAME);
        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/{id}", gardenField.getId() + 1)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void deleteGardenField() throws Exception {
        GardenField gardenField = createAndSaveGardenField(GARDEN_NAME);

        int databaseSizeBeforeDelete = gardenFieldRepository.findAll().size();

        restGardenFieldMockMvc.perform(delete("/api/v1/gardenfields/{id}", gardenField.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    @WithMockUser(value = DEFAULT_LOGIN)
    public void deleteNonExistingGardenField() throws Exception {
        int databaseSizeBeforeDelete = gardenFieldRepository.findAll().size();

        restGardenFieldMockMvc.perform(delete("/api/v1/gardenfields/{id}", 9999)
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        List<GardenField> gardenFields = gardenFieldRepository.findAll();
        assertThat(gardenFields).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void getGardenFieldBoundaries() throws Exception {
        GardenField gardenField1 = createGardenField("Garden1");
        GardenField gardenField2 = createGardenField("Garden2");
        Double priceOne = 99.0;
        Double priceTwo = 100.0;
        Double sizeOne = 10.0;
        Double sizeTwo = 11.0;
        gardenField1.setPricePerM2(priceOne);
        gardenField1.setSizeInM2(sizeOne);
        gardenField2.setPricePerM2(priceTwo);
        gardenField2.setSizeInM2(sizeTwo);
        // calculate price per gardenfield (priceperm2 * sizeinm2 * 30)
        Double priceOnePerMonth = priceOne * sizeOne * 30;
        Double priceTwoPerMonth = priceTwo * sizeTwo * 30;
        Double minPrice = priceOnePerMonth < priceTwoPerMonth ? priceOnePerMonth : priceTwoPerMonth;
        Double maxPrice = priceOnePerMonth > priceTwoPerMonth ? priceOnePerMonth : priceTwoPerMonth;

        gardenFieldRepository.save(gardenField1);
        gardenFieldRepository.saveAndFlush(gardenField2);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/filterBoundaries")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.minPrice").value(is(minPrice)))
            .andExpect(jsonPath("$.maxPrice").value(is(maxPrice)))
            .andExpect(jsonPath("$.minSize").value(is(sizeOne)))
            .andExpect(jsonPath("$.maxSize").value(is(sizeTwo)));
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

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getLeasingsForGardenField() throws Exception {
        GardenField gardenField1 = createAndSaveGardenField("gardenField1");
        GardenField gardenField2 = createAndSaveGardenField("gardenField2");

        Leasing leasing1 = new Leasing();
        leasing1.setUser(user);
        leasing1.setGardenField(gardenField1);
        leasing1.setFrom(Instant.now().plus(10, ChronoUnit.DAYS));
        leasing1.setTo(Instant.now().plus(20, ChronoUnit.DAYS));
        leasing1.setStatus(LeasingStatus.RESERVED);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing1);

        Leasing leasing2 = new Leasing();
        leasing2.setUser(user);
        leasing2.setGardenField(gardenField2);
        leasing2.setFrom(Instant.now().plus(21, ChronoUnit.DAYS));
        leasing2.setTo(Instant.now().plus(30, ChronoUnit.DAYS));
        leasing2.setStatus(LeasingStatus.RESERVED);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing2);

        Leasing leasing3 = new Leasing();
        leasing3.setUser(user);
        leasing3.setGardenField(gardenField1);
        leasing3.setFrom(Instant.now().plus(31, ChronoUnit.DAYS));
        leasing3.setTo(Instant.now().plus(40, ChronoUnit.DAYS));
        leasing3.setStatus(LeasingStatus.RESERVED);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing3);

        assertThat(leasingRepository.findAll()).hasSize(3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/{gardenFieldId}/leasings", gardenField1.getId()))
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
        GardenField gardenField1 = createAndSaveGardenField("gardenField1");
        GardenField gardenField2 = createAndSaveGardenField("gardenField2");

        Leasing leasing1 = new Leasing();
        leasing1.setUser(user);
        leasing1.setGardenField(gardenField1);
        leasing1.setFrom(Instant.now().plus(10, ChronoUnit.DAYS));
        leasing1.setTo(Instant.now().plus(20, ChronoUnit.DAYS));
        leasing1.setStatus(LeasingStatus.OPEN);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing1);

        Leasing leasing2 = new Leasing();
        leasing2.setUser(user);
        leasing2.setGardenField(gardenField2);
        leasing2.setFrom(Instant.now().plus(21, ChronoUnit.DAYS));
        leasing2.setTo(Instant.now().plus(30, ChronoUnit.DAYS));
        leasing2.setStatus(LeasingStatus.RESERVED);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing2);

        Leasing leasing3 = new Leasing();
        leasing3.setUser(user);
        leasing3.setGardenField(gardenField1);
        leasing3.setFrom(Instant.now().plus(31, ChronoUnit.DAYS));
        leasing3.setTo(Instant.now().plus(40, ChronoUnit.DAYS));
        leasing3.setStatus(LeasingStatus.RESERVED);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing3);

        assertThat(leasingRepository.findAll()).hasSize(3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/{gardenFieldId}/leasings?leasingStatus=" + LeasingStatus.OPEN.toString(), gardenField1.getId()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(1)))
            .andExpect(jsonPath("$.content.[0].id").value(is(leasing1.getId().intValue())));
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getLeasingsForUserWithTwoStatusFilter() throws Exception {
        GardenField gardenField1 = createAndSaveGardenField("gardenField1");

        Leasing leasing1 = new Leasing();
        leasing1.setUser(user);
        leasing1.setGardenField(gardenField1);
        leasing1.setFrom(Instant.now().plus(10, ChronoUnit.DAYS));
        leasing1.setTo(Instant.now().plus(20, ChronoUnit.DAYS));
        leasing1.setStatus(LeasingStatus.OPEN);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing1);

        Leasing leasing2 = new Leasing();
        leasing2.setUser(user);
        leasing2.setGardenField(gardenField1);
        leasing2.setFrom(Instant.now().plus(21, ChronoUnit.DAYS));
        leasing2.setTo(Instant.now().plus(30, ChronoUnit.DAYS));
        leasing2.setStatus(LeasingStatus.RESERVED);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing2);

        Leasing leasing3 = new Leasing();
        leasing3.setUser(user);
        leasing3.setGardenField(gardenField1);
        leasing3.setFrom(Instant.now().plus(31, ChronoUnit.DAYS));
        leasing3.setTo(Instant.now().plus(40, ChronoUnit.DAYS));
        leasing3.setStatus(LeasingStatus.CANCELLED);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/{gardenFieldId}/leasings?leasingStatus="
            + LeasingStatus.OPEN.toString() + "&leasingStatus=" + LeasingStatus.CANCELLED.toString(), gardenField1.getId()))
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
    public void getLeasingsForUserWithStatusAndFromFilter() throws Exception {
        GardenField gardenField1 = createAndSaveGardenField("gardenField1");

        Leasing leasing1 = new Leasing();
        leasing1.setUser(user);
        leasing1.setGardenField(gardenField1);
        leasing1.setFrom(Instant.now().plus(10, ChronoUnit.DAYS));
        leasing1.setTo(Instant.now().plus(20, ChronoUnit.DAYS));
        leasing1.setStatus(LeasingStatus.OPEN);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing1);

        Leasing leasing2 = new Leasing();
        leasing2.setUser(user);
        leasing2.setGardenField(gardenField1);
        leasing2.setFrom(Instant.now().plus(21, ChronoUnit.DAYS));
        leasing2.setTo(Instant.now().plus(30, ChronoUnit.DAYS));
        leasing2.setStatus(LeasingStatus.OPEN);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing2);

        Leasing leasing3 = new Leasing();
        leasing3.setUser(user);
        leasing3.setGardenField(gardenField1);
        leasing3.setFrom(Instant.now().plus(31, ChronoUnit.DAYS));
        leasing3.setTo(Instant.now().plus(40, ChronoUnit.DAYS));
        leasing3.setStatus(LeasingStatus.OPEN);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/{gardenFieldId}/leasings?leasingStatus="
            + LeasingStatus.OPEN.toString() + "&from=" + leasing2.getFrom(), gardenField1.getId()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(2)))
            .andExpect(jsonPath("$.content.[0].id").value(is(leasing2.getId().intValue())))
            .andExpect(jsonPath("$.content.[1].id").value(is(leasing3.getId().intValue())));
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getLeasingsForUserWithStateFilter() throws Exception {
        GardenField gardenField1 = createAndSaveGardenField("gardenField1");
        GardenField gardenField2 = createAndSaveGardenField("gardenField2");

        Leasing leasing1 = new Leasing();
        leasing1.setUser(user);
        leasing1.setGardenField(gardenField1);
        leasing1.setFrom(Instant.now().minus(20, ChronoUnit.DAYS));
        leasing1.setTo(Instant.now().minus(10, ChronoUnit.DAYS));
        leasing1.setStatus(LeasingStatus.OPEN);
        leasing1.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing1);

        Leasing leasing2 = new Leasing();
        leasing2.setUser(user);
        leasing2.setGardenField(gardenField2);
        leasing2.setFrom(Instant.now().plus(21, ChronoUnit.DAYS));
        leasing2.setTo(Instant.now().plus(30, ChronoUnit.DAYS));
        leasing2.setStatus(LeasingStatus.RESERVED);
        leasing2.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing2);

        Leasing leasing3 = new Leasing();
        leasing3.setUser(user);
        leasing3.setGardenField(gardenField1);
        leasing3.setFrom(Instant.now().plus(31, ChronoUnit.DAYS));
        leasing3.setTo(Instant.now().plus(40, ChronoUnit.DAYS));
        leasing3.setStatus(LeasingStatus.RESERVED);
        leasing3.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.save(leasing3);

        assertThat(leasingRepository.findAll()).hasSize(3);

        restGardenFieldMockMvc.perform(get("/api/v1/gardenfields/{gardenFieldId}/leasings?state=" + LeasingState.PAST.toString(), gardenField1.getId()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(is(1)))
            .andExpect(jsonPath("$.content.[0].id").value(is(leasing1.getId().intValue())));
    }

    private GardenField createAndSaveGardenField(String gardenName) {
        return gardenFieldRepository.saveAndFlush(createGardenField(gardenName));
    }

    private GardenField createGardenField(String gardenName) {
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
        return gardenField;
    }


    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createGardenFieldWithScriptShouldThrowInvalidException() throws Exception {
        GardenFieldDTO gardenFieldDTO = createGardenFieldDTOWithoutOwner();
        gardenFieldDTO.setDescription("<script>alert('XSS')</script>");
        gardenFieldDTO.setOwner(simpleUserDTO);

        restGardenFieldMockMvc.perform(post("/api/v1/gardenfields")
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isBadRequest());
    }
}
