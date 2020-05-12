package com.gardle.datagenerator;

import com.gardle.config.ImageStorageProperties;
import com.gardle.repository.*;
import com.gardle.service.PaymentService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Profile("generateQuickData")
@Component
public class QuickDataGenerator extends DataGenerator {
    private static final int NUMBER_OF_GARDEN_FIELDS_TO_GENERATE = 100;
    private static final int MAX_NUMBER_OF_IMAGES_PER_GARDENFIELD = 6;
    private static final int MIN_NUMBER_OF_IMAGES_PER_GARDENFIELD = 1;
    private static final int NUMBER_OF_USERS_TO_GENERATE = 30;
    private static final int NUMBER_OF_OPEN_LEASINGS_TO_GENERATE = 0;
    private static final int NUMBER_OF_RESERVED_LEASINGS_TO_GENERATE = 150;
    private static final int NUMBER_OF_MESSAGES_THREADS = 270;
    private static final int NUMBER_OF_MESSAGES = 100;
    private static final int BATCH_SIZE = 50; //Should be the same as configured in the application-generateQuickData.yml

    public QuickDataGenerator(GardenFieldRepository gardenFieldRepository, UserRepository userRepository,
                              LeasingRepository leasingRepository, AuthorityRepository authorityRepository,
                              MessageRepository messageRepository, ImageStorageProperties imageStorageProperties,
                              PaymentService paymentService) {
        super(gardenFieldRepository, userRepository, leasingRepository, authorityRepository,
            messageRepository, imageStorageProperties, paymentService);
    }

    @PostConstruct
    private void generateData() {
        super.generate(NUMBER_OF_GARDEN_FIELDS_TO_GENERATE, NUMBER_OF_USERS_TO_GENERATE, NUMBER_OF_OPEN_LEASINGS_TO_GENERATE,
            NUMBER_OF_RESERVED_LEASINGS_TO_GENERATE, NUMBER_OF_MESSAGES_THREADS, NUMBER_OF_MESSAGES,
            MIN_NUMBER_OF_IMAGES_PER_GARDENFIELD, MAX_NUMBER_OF_IMAGES_PER_GARDENFIELD, BATCH_SIZE);
    }
}
