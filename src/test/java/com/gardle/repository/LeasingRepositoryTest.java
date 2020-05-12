package com.gardle.repository;

import com.gardle.GardleApp;
import com.gardle.domain.GardenField;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.LeasingStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = GardleApp.class)
@Transactional
public class LeasingRepositoryTest {

    @Autowired
    private LeasingRepository leasingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GardenFieldRepository gardenFieldRepository;

    private static final String DEFAULT_IBAN = "AT022050302101023600";
    private static final String DEFAULT_PAYMENT_SESSION_ID = "cs_test_m5CBqcXSIJeKW7Ijb5vp8D9BrvDlJ6lRn25m6BdTZ0a1cLbIz5xVQ7bX";

    @Test
    public void testSoftDeletedEntityShouldNotBeRetrieved() {
        User user = new User();
        user.setLogin("testlogin");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + "@test.com");
        user.setFirstName("test");
        user.setLastName("test");
        user.setBankAccountIBAN(DEFAULT_IBAN);
        user = userRepository.saveAndFlush(user);

        GardenField gardenField = new GardenField();
        gardenField.setName("test field");
        gardenField.setCity("test city");
        gardenField.setLongitude(1.0);
        gardenField.setLatitude(1.0);
        gardenField.setOwner(user);
        gardenField.setPricePerM2(1.0);
        gardenField.setSizeInM2(10.0);

        gardenFieldRepository.saveAndFlush(gardenField);

        leasingRepository.deleteAll();
        Leasing leasing = new Leasing();
        leasing.from(Instant.now());
        leasing.to(Instant.now().plusSeconds(10000));
        leasing.status(LeasingStatus.OPEN);
        leasing.setUser(user);
        leasing.setGardenField(gardenField);
        leasing.setPaymentSessionId(DEFAULT_PAYMENT_SESSION_ID);
        leasingRepository.saveAndFlush(leasing);

        assertThat(leasingRepository.findAll()).containsExactly(leasing);

        leasing.setDeleted(Instant.now());
        leasingRepository.saveAndFlush(leasing);

        assertThat(leasingRepository.findAll()).isEmpty();

    }
}
