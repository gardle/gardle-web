package com.gardle.repository;

import com.gardle.GardleApp;
import com.gardle.domain.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = GardleApp.class)
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    private static final String DEFAULT_IBAN = "AT022050302101023600";


    @Test
    public void testSoftDeletedEntityShouldNotBeRetrieved() {
        userRepository.deleteAll();
        User user = new User();
        user.setLogin("testlogin");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + "@test.com");
        user.setFirstName("test");
        user.setLastName("test");
        user.setBankAccountIBAN(DEFAULT_IBAN);
        user = userRepository.saveAndFlush(user);

        assertThat(userRepository.findAll()).containsExactly(user);

        user.setDeleted(Instant.now());
        userRepository.saveAndFlush(user);

        assertThat(userRepository.findAll()).isEmpty();

    }
}
