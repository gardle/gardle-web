package com.gardle.service;

import com.gardle.GardleApp;
import com.gardle.domain.GardenField;
import com.gardle.domain.User;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.dto.AbstractCheckoutDTO;
import com.gardle.service.dto.CreatePaymentProviderAccountDTO;
import com.gardle.service.dto.GetStripeAccountLinkUrlDTO;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import com.stripe.Stripe;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Integration test for the stripe API, reference for testing: https://stripe.com/docs/testing
 */
@SpringBootTest(classes = GardleApp.class)
@Transactional
public class StripeServiceIT {

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
    private static final String OWNER_LOGIN = "johndoe";
    private static final String DEFAULT_EMAIL = "johndoe@localhost.com";
    private static final String DEFAULT_IBAN = "AT89370400440532013000";

    private CreatorLeasingDTO creatorLeasingDTO;
    @Autowired
    private StripeService stripeService;
    @Value("${stripe.private-key}")
    private String stripeApiKey;

    @Autowired
    private GardenFieldRepository gardenFieldRepository;
    @Autowired
    private UserRepository userRepository;

    private User owner;
    private GardenField gardenField;

    @BeforeEach
    public void init() {
        createOwner();
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
        gardenFieldRepository.save(gardenField);

        creatorLeasingDTO = new CreatorLeasingDTO();
        creatorLeasingDTO.setGardenFieldId(gardenField.getId());
        creatorLeasingDTO.setFrom(Instant.now().plus(100, ChronoUnit.DAYS));
        creatorLeasingDTO.setTo(creatorLeasingDTO.getFrom().plus(100, ChronoUnit.DAYS));
        creatorLeasingDTO.setGardenFieldName("gardenfield name");

        Stripe.apiKey = stripeApiKey;
    }

    @Test
    @WithMockUser(OWNER_LOGIN)
    public void testCreateCheckoutSession() {
        AbstractCheckoutDTO result = stripeService.createCheckoutSession(creatorLeasingDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }

    @Test
    public void testCreateAccount() {

        String res = stripeService.createAccount(new CreatePaymentProviderAccountDTO(DEFAULT_EMAIL, DEFAULT_IBAN));
        assertThat(res).isNotNull();
        assertThat(res).isNotEmpty();
    }

    @Test
    public void testGetAccountLinkUrl() {
        String res = stripeService.getAccountLinkUrl(new GetStripeAccountLinkUrlDTO(stripeService.createAccount(new CreatePaymentProviderAccountDTO(DEFAULT_EMAIL, DEFAULT_IBAN)),
            ""));
        assertThat(res).isNotNull();
        assertThat(res).isNotEmpty();
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
        userRepository.save(owner);
    }
}
