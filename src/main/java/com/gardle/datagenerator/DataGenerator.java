package com.gardle.datagenerator;

import com.gardle.config.ImageStorageProperties;
import com.gardle.domain.*;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.domain.enumeration.MessageType;
import com.gardle.repository.*;
import com.gardle.service.PaymentService;
import com.github.javafaker.Faker;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class DataGenerator {
    private static final Logger log = LoggerFactory.getLogger(DataGenerator.class);

    private static final String DEFAULT_USER_PASSWORD = "$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K";
    private static final String DEFAULT_ADMIN_PASSWORD = "$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC";
    private static final String USER_ROLE = "ROLE_USER";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String DEFAULT_IBAN = "AT89370400440532013000";

    private int numberOfGardenFields;
    private int numberOfUsers;
    private int numberOfOpenLeasings;
    private int numberOfReservedLeasings; //next year may, june, july, august
    private int numberOfMessagesThreads;
    private int numberOfMessages;
    private int maxNumberOfImagesPerGardenField;
    private int minNumberOfImagesPerGardenField;
    private int batchSize;

    private Set<Authority> authorities;
    private List<User> users;
    private List<GardenField> gardenFields;

    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final GardenFieldRepository gardenFieldRepository;
    private final LeasingRepository leasingRepository;
    private final MessageRepository messageRepository;

    private final Faker faker;
    private final Path imageStoragePath;
    private final String thumbnailFolderName;
    private final Random random;
    private final PaymentService paymentService;
    private final ClassLoader classLoader;

    public DataGenerator(GardenFieldRepository gardenFieldRepository, UserRepository userRepository,
                         LeasingRepository leasingRepository, AuthorityRepository authorityRepository,
                         MessageRepository messageRepository, ImageStorageProperties imageStorageProperties,
                         PaymentService paymentService) {
        this.gardenFieldRepository = gardenFieldRepository;
        this.userRepository = userRepository;
        this.leasingRepository = leasingRepository;
        this.authorityRepository = authorityRepository;
        this.messageRepository = messageRepository;
        this.imageStoragePath = Paths.get(imageStorageProperties.getImageDir());
        this.thumbnailFolderName = imageStorageProperties.THUMBNAIL_FOLDER_NAME;
        this.classLoader = getClass().getClassLoader();
        this.paymentService = paymentService;

        faker = new Faker();
        users = new ArrayList<>();
        gardenFields = new ArrayList<>();
        authorities = new HashSet<>();
        random = new Random();
    }

    public void generate(int numberOfGardenFields, int numberOfUsers, int numberOfOpenLeasings,
                         int numberOfReservedLeasings, int numberOfMessagesThreads, int numberOfMessages,
                         int minNumberOfImagesPerGardenField, int maxNumberOfImagesPerGardenField, int batchSize) {
        this.numberOfGardenFields = numberOfGardenFields;
        this.numberOfUsers = numberOfUsers;
        this.numberOfOpenLeasings = numberOfOpenLeasings;
        this.numberOfReservedLeasings = numberOfReservedLeasings;
        this.numberOfMessagesThreads = numberOfMessagesThreads;
        this.numberOfMessages = numberOfMessages;
        this.minNumberOfImagesPerGardenField = minNumberOfImagesPerGardenField;
        this.maxNumberOfImagesPerGardenField = maxNumberOfImagesPerGardenField;
        this.batchSize = batchSize;

        boolean isGeneratorInstance = generateAuthorities();
        if (isGeneratorInstance) {
            log.info("This instance will be generating data");
            boolean success = generateAdmin() &&
                generateUsers() &&
                generateGardenFields() &&
                generateGardenFieldImages() &&
                generateLeasings() &&
                generateMessages();
            if(success){
                log.info("Data generation finished successfully");
            } else {
                log.error("Data generation failed.");
            }

        } else {
            log.info("This instance will NOT generate data. Maybe an other instance is currently generating the data or the data was already generated in the past.");
        }
    }

    private boolean generateAuthorities() {
        authorities = new HashSet<>(authorityRepository.findAll());
        if (authorities.size() > 0) {
            log.info("Authorities were already generated");
            return false;
        }
        log.info("Start generating authorities");
        Authority roleUser = new Authority();
        roleUser.setName(USER_ROLE);
        Authority roleAdmin = new Authority();
        roleAdmin.setName(ADMIN_ROLE);
        authorities.add(authorityRepository.save(roleAdmin));
        authorities.add(authorityRepository.save(roleUser));
        log.info("Generated {} authorities", authorities.size());
        return true;
    }

    private boolean generateAdmin() {
        User administrator = userRepository.findOneWithAuthoritiesByLogin("Administrator").orElse(null);
        if (administrator != null) {
            log.info("Administator was already generated");
            return false;
        }
        log.info("Start generating Administrator");
        administrator = new User();
        administrator.setFirstName("Administrator");
        administrator.setLastName("Admin");
        administrator.setLogin("Administrator");
        administrator.setEmail("admin@gardle.ga");
        administrator.setPassword(DEFAULT_ADMIN_PASSWORD);
        administrator.setActivated(true);
        administrator.setAuthorities(authorities);
        administrator.setBankAccountIBAN(DEFAULT_IBAN);
        administrator.setStripeAccountId(paymentService.createPaymentAccount(administrator.getEmail(), administrator.getBankAccountIBAN()));
        administrator.setStripeAccountVerified(true);
        log.info("Administrator is generated");
        return true;
    }

    private boolean generateUsers() {
        users = userRepository.findAll();
        if (users.size() > 1) {
            log.info("Users were already generated");
            return false;
        }
        return generateRandomUsers();
    }

    private boolean generateRandomUsers() {
        Authority userAuthority = authorityRepository.findById(USER_ROLE).orElse(null);
        if (userAuthority == null) {
            log.info("Users generation was skipped because authorities generation is not finished yet");
            return false;
        }
        log.info("Start generating {} users", numberOfUsers);
        HashSet<Authority> userAuthorities = new HashSet<>();
        userAuthorities.add(userAuthority);
        List<User> batchedUser = new LinkedList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            if (i % batchSize == 0) {
                userRepository.saveAll(batchedUser);
                batchedUser.clear();
            }
            User user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            String login = faker.name().username() + i;
            if (i < 5) {
                user.setLogin("user" + i);
            } else {
                user.setLogin(login);
            }
            user.setEmail(login + "@gmail.com");
            user.setPassword(DEFAULT_USER_PASSWORD);
            user.setActivated(true);
            user.setAuthorities(userAuthorities);
            user.setBankAccountIBAN(DEFAULT_IBAN);
            user.setStripeAccountId(paymentService.createPaymentAccount(user.getEmail(), user.getBankAccountIBAN()));
            user.setStripeAccountVerified(true);
            User savedUser = userRepository.save(user);
            batchedUser.add(savedUser);
            users.add(savedUser);
        }
        userRepository.saveAll(batchedUser);
        log.info("Generated {} users", numberOfUsers);
        return true;
    }

    private boolean generateGardenFields() {
        if (gardenFieldRepository.count() > 0) {
            log.info("Gardenfields were already generated");
            return false;
        }
        if (users.size() < numberOfUsers) {
            log.info("Skipping gardenfield generation because users generation is not finished yet or an other instance is working on the data generation");
            return false;
        }
        log.info("Start generating {} gardenfields", numberOfGardenFields);
        List<GardenField> gardenFieldBatch = new LinkedList<>();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < numberOfGardenFields; i++) {
            if (i % batchSize == 0) {
                gardenFieldRepository.saveAll(gardenFieldBatch);
                gardenFieldBatch.clear();
            }
            GardenField gardenField = new GardenField();
            gardenField.setName(faker.app().name());
            String description = faker.lorem().paragraph(faker.number().numberBetween(3, 12));
            if (description.length() > 1000) {
                description = description.substring(0, 1000);
            }
            gardenField.setDescription(description);
            gardenField.setSizeInM2(faker.number().randomDouble(2, 3, 400));
            gardenField.setPricePerM2(random.nextDouble() * 1.8 / 50);
            gardenField.setLatitude((random.nextDouble() / 6) + 48.116);
            gardenField.setLongitude((random.nextDouble() / 2.5) + 16.18);
            gardenField.setCity(faker.address().city());
            gardenField.setElectricity(faker.bool().bool());
            gardenField.setWater(faker.bool().bool());
            gardenField.setHigh(faker.bool().bool());
            gardenField.setGlassHouse(faker.bool().bool());
            gardenField.setRoofed(faker.bool().bool());
            gardenField.setOwner(users.get(faker.number().numberBetween(0, users.size() - 1)));
            gardenField = gardenFieldRepository.save(gardenField);
            gardenFieldBatch.add(gardenField);
            gardenFields.add(gardenField);
        }
        log.info("Generated {} gardenfields", gardenFields.size());
        return true;
    }

    private boolean generateGardenFieldImages() {
        try {
            log.info("Start generating gardenfield images for {} gardenfields", gardenFields.size());
            List<Path> allDummyImage = getAllDummyImage();
            Set<Long> skippedIds = new HashSet<>();
            for (GardenField gardenField : this.gardenFields) {
                Path gardenFieldPath = this.imageStoragePath.resolve(String.valueOf(gardenField.getId()));
                Path thumbnailPath = gardenFieldPath.resolve(this.thumbnailFolderName);

                if (gardenFieldPath.toFile().exists()) {
                    skippedIds.add(gardenField.getId());
                    continue;
                }

                Files.createDirectories(gardenFieldPath);
                Files.createDirectories(thumbnailPath);

                List<Path> randomlySelectedImages = selectRandomImages(allDummyImage);
                List<String> generatedFilenames = generateImagesForGardenField(gardenFieldPath, randomlySelectedImages);
                if (generatedFilenames.size() > 0) {
                    gardenField.setCoverImage(generatedFilenames.get(0));
                    gardenFieldRepository.save(gardenField);
                }
            }
            log.info("Generated images for {} gardenfields and skipped generation for {} gardenfields {}", gardenFields.size() -  skippedIds.size(), skippedIds.size(), skippedIds );
            return true;
        } catch (Exception e) {
            log.error("Generating gardenfield images FAILED", e);
            return false;
        }
    }

    private List<Path> getAllDummyImage() throws Exception {
        try {
            URL dummyImagesResource = this.classLoader.getResource("dummy-images");
            Path dummyImagesPath = Paths.get(Objects.requireNonNull(dummyImagesResource).toURI());
            return Files.walk(dummyImagesPath, 1).filter(Files::isRegularFile).collect(Collectors.toList());
        } catch (Exception ex) {
            throw new Exception("Could not read all filenames of dummy images in resource folder", ex);
        }
    }

    private List<Path> selectRandomImages(List<Path> allImages) {
        int imageCount = Math.min(allImages.size(), faker.number().numberBetween(this.minNumberOfImagesPerGardenField, this.maxNumberOfImagesPerGardenField + 1)); // plus one because the nextInt bound is exclusive
        return random.ints(0, allImages.size()).distinct().limit(imageCount)
            .mapToObj(allImages::get)
            .collect(Collectors.toList());
    }

    private List<String> generateImagesForGardenField(Path gardenFieldPath, List<Path> randomlySelectedImages) throws IOException {
        List<String> savedImages = new ArrayList<>();
        for (Path dummyImagePath : randomlySelectedImages) {
            Path dummyThumbnailImagePath = dummyImagePath.getParent().resolve(this.thumbnailFolderName).resolve(dummyImagePath.getFileName());
            String fileExtension = FilenameUtils.getExtension(dummyImagePath.getFileName().toString());
            String newFilename = String.format("%s.%s", UUID.randomUUID(), fileExtension);
            Path targetImagePath = gardenFieldPath.resolve(newFilename);
            Path targetThumbnailImagePath = gardenFieldPath.resolve(this.thumbnailFolderName).resolve(newFilename);
            Files.copy(dummyImagePath, targetImagePath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(dummyThumbnailImagePath, targetThumbnailImagePath, StandardCopyOption.REPLACE_EXISTING);
            savedImages.add(newFilename);
        }
        return savedImages;
    }

    private boolean generateLeasings() {
        if (leasingRepository.count() > 0) {
            log.info("Leasings were already generated");
            return false;
        }
        if (users.size() < numberOfUsers || gardenFields.size() < numberOfGardenFields) {
            log.info("Skipping leasing generation because users or gardenfields are missing or an other instance is working on the data generation");
            return false;
        }

        log.info("Generating {} leasing entries", numberOfReservedLeasings + numberOfOpenLeasings);
        List<Leasing> leasings = new LinkedList<>();
        for (int i = 0; i < numberOfOpenLeasings; i++) {
            if (i % batchSize == 0) {
                leasingRepository.saveAll(leasings);
                leasings.clear();
            }
            Leasing leasing = new Leasing();
            leasing.setFrom(faker.date().future(10, TimeUnit.DAYS).toInstant());
            leasing.setTo(faker.date().future(100, TimeUnit.DAYS, Date.from(leasing.getFrom())).toInstant());
            leasing.setGardenField(gardenFields.get(faker.number().numberBetween(0, gardenFields.size() - 1)));
            leasing.setStatus(LeasingStatus.OPEN);
            leasing.setUser(users.get(faker.number().numberBetween(0, users.size() - 1)));
            leasing.setPaymentSessionId("cs_test_m5CBqcXSIJeKW7Ijb5vp8D9BrvDlJ6lRn25m6BdTZ0a1cLbIz5xVQ7bX");
            leasings.add(leasing);
        }
        leasingRepository.saveAll(leasings);
        leasings.clear();
        //create reserved leasings for the future
        int reservedLeasingForThisGardenfield = numberOfReservedLeasings / gardenFields.size();
        int remainingLeasings = (numberOfReservedLeasings % gardenFields.size()) + (gardenFields.size() % 2 == 0 ? 0 : -1);
        for (int i = 0; i < gardenFields.size(); i++) {
            int leasingCount = reservedLeasingForThisGardenfield + (i < remainingLeasings ? 1 : 0) + (i % 2 == 0 ? 1 : -1);
            Instant start = faker.date().future(7, TimeUnit.DAYS).toInstant();
            for (int j = 0; j < leasingCount; j++) {
                start = start.plus(Duration.ofDays(faker.number().numberBetween(1, 180)));
                Leasing leasing = new Leasing();
                leasing.setStatus(LeasingStatus.RESERVED);
                leasing.setFrom(start);
                start = start.plus(Duration.ofDays(faker.number().numberBetween(100, 450)));
                leasing.setTo(start);
                leasing.setGardenField(gardenFields.get(i));
                leasing.setUser(users.get(faker.number().numberBetween(0, users.size() - 1)));
                leasing.setPaymentSessionId("cs_test_m5CBqcXSIJeKW7Ijb5vp8D9BrvDlJ6lRn25m6BdTZ0a1cLbIz5xVQ7bX");
                leasings.add(leasing);
                if (leasings.size() >= batchSize) {
                    leasingRepository.saveAll(leasings);
                    leasings.clear();
                }
            }
        }
        leasingRepository.saveAll(leasings);
        log.info("Generated {} leasings", leasingRepository.count());
        return true;
    }


    private boolean generateMessages() {
        if (messageRepository.count() > 0) {
            log.info("Messages were already generated");
            return false;
        }

        if (users.size() < numberOfUsers) {
            log.info("Skipping message generation because users generation is not finished or an other instance is working on the data generation");
            return false;
        }

        log.info("Generating {} messages in {} threads", numberOfMessages, numberOfMessagesThreads);
        HashMap<Long, Set<Long>> threadsWithOtherUsers = new HashMap<>();
        for (int i = 0; i < numberOfMessagesThreads; i++) {
            UUID threadId = UUID.fromString(faker.internet().uuid());

            User userFrom = users.get(faker.number().numberBetween(0, users.size() - 1));
            User userTo = users.get(faker.number().numberBetween(0, users.size() - 1));
            while (userFrom.equals(userTo)) {
                userTo = users.get(faker.number().numberBetween(0, users.size() - 1));
            }

            while (threadsWithOtherUsers.get(userFrom.getId()) != null && threadsWithOtherUsers.get(userFrom.getId()).contains(userTo.getId())) {
                userFrom = users.get(faker.number().numberBetween(0, users.size() - 1));
                userTo = users.get(faker.number().numberBetween(0, users.size() - 1));
                while (userFrom.equals(userTo)) {
                    userTo = users.get(faker.number().numberBetween(0, users.size() - 1));
                }
            }

            // Make sure there are not two or more threads between two users
            threadsWithOtherUsers.putIfAbsent(userFrom.getId(), new HashSet<>());
            threadsWithOtherUsers.putIfAbsent(userTo.getId(), new HashSet<>());
            threadsWithOtherUsers.get(userFrom.getId()).add(userTo.getId());
            threadsWithOtherUsers.get(userTo.getId()).add(userFrom.getId());

            List<Message> messages = new LinkedList<>();
            for (int j = 0; j < numberOfMessages; j++) {
                if (j % batchSize == 0) {
                    messageRepository.saveAll(messages);
                    messages.clear();
                }
                Message message = new Message();
                message.setThread(threadId);
                message.setContent(faker.lorem().sentence(faker.number().numberBetween(1, 5)));
                message.setType(MessageType.USER);
                User firstUser = faker.bool().bool() ? userTo : userFrom;
                message.setUserFrom(firstUser);
                message.setUserTo(firstUser.getId().equals(userFrom.getId()) ? userTo : userFrom);
                message.setCreatedDate(faker.date().past(21600, TimeUnit.MINUTES).toInstant()); //Random timestamp up to 15 days in the past
                message.setLastModifiedDate(message.getCreatedDate());
                message.setOpened(false);
                messages.add(message);
            }
            messageRepository.saveAll(messages);
        }
        log.info("Generated all messages");
        return true;
    }
}
