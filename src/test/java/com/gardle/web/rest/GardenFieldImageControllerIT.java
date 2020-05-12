package com.gardle.web.rest;

import com.gardle.GardleApp;
import com.gardle.config.ImageStorageProperties;
import com.gardle.domain.GardenField;
import com.gardle.domain.User;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.ImageStorageService;
import com.gardle.web.rest.errors.ExceptionTranslator;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GardleApp.class)
public class GardenFieldImageControllerIT {

    private MockMvc restFileMockMvc;

    private final ResourceHttpMessageConverter resourceHttpMessageConverter = new ResourceHttpMessageConverter();

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private ImageStorageService imageStorageService;

    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String DEFAULT_EMAIL = "johndoe@localhost";
    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String DEFAULT_LASTNAME = "doe";
    private static final String GARDEN_NAME = "testGarden";
    private static final Double SIZE_IN_M2 = 10.0;
    private static final Double PRICE_PER_M2 = 3.0;
    private static final Double LATITUDE = 1.0;
    private static final Double LONGITUDE = 2.0;
    private static final String CITY = "testCity";
    private static final String DEFAULT_IBAN = "AT89370400440532013000";


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GardenFieldRepository gardenFieldRepository;

    private GardenField gardenField;

    private Path gardenFieldPath;

    @Autowired
    private ImageStorageProperties imageStorageProperties;

    @BeforeEach
    public void setupGardenAndUser() {
        User user = new User();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setBankAccountIBAN(DEFAULT_IBAN);

        gardenField = new GardenField();
        gardenField.setOwner(userRepository.save(user));
        gardenField.setName(GARDEN_NAME);
        gardenField.setSizeInM2(SIZE_IN_M2);
        gardenField.setPricePerM2(PRICE_PER_M2);
        gardenField.setLatitude(LATITUDE);
        gardenField.setLongitude(LONGITUDE);
        gardenField.setCity(CITY);
        gardenFieldRepository.saveAndFlush(gardenField);

        gardenFieldPath = Paths.get(imageStorageProperties.getImageDir()).resolve(gardenField.getId().toString());
    }

    @BeforeEach
    public void setup() throws IOException {
        GardenFieldImageController gardenFieldImageController = new GardenFieldImageController(imageStorageService);

        this.restFileMockMvc = MockMvcBuilders.standaloneSetup(gardenFieldImageController)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter, resourceHttpMessageConverter)
            .build();

        FileUtils.deleteDirectory(Paths.get(imageStorageProperties.getImageDir()).toFile());
    }

    @WithMockUser(DEFAULT_LOGIN)
    @Test
    @Transactional
    public void uploadImage() throws Exception {
        String imageName = uploadImageSuccessfully();

        File imageTest = gardenFieldPath.resolve(imageName).toFile();
        assertThat(imageTest.exists()).isTrue();
        assertThat(imageTest.delete()).isTrue();
    }

    @Test
    @Transactional
    @WithAnonymousUser
    public void uploadImageWithoutAuthority() throws Exception {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("images/logo.png")).getFile());
        MockMultipartFile image = new MockMultipartFile("image", "logo.png", "image/png", FileUtils.readFileToByteArray(file));

        restFileMockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/gardenfields/" + gardenField.getId() + "/uploadImage")
            .file(image))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.imageName").doesNotExist());

        assertThat(gardenFieldPath.toFile().exists()).isFalse();
    }

    @WithMockUser(DEFAULT_LOGIN)
    @Test
    @Transactional
    public void uploadImageToNotExistingGardenField() throws Exception {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("images/logo.png")).getFile());
        MockMultipartFile image = new MockMultipartFile("image", "logo.png", "image/png", FileUtils.readFileToByteArray(file));

        restFileMockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/gardenfields/" + (gardenField.getId() + 1) + "/uploadImage")
            .file(image))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.imageName").doesNotExist());

        assertThat(gardenFieldPath.toFile().exists()).isFalse();
    }

    @WithMockUser(DEFAULT_LOGIN)
    @Test
    @Transactional
    public void uploadImageWithNotSupportedFileFormat() throws Exception {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("images/test.txt")).getFile());
        MockMultipartFile textFile = new MockMultipartFile("image", "test.txt", "text/plain", FileUtils.readFileToByteArray(file));

        restFileMockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/gardenfields/" + gardenField.getId() + "/uploadImage")
            .file(textFile))
            .andExpect(status().isBadRequest());

        assertThat(gardenFieldPath.toFile().exists()).isFalse();
    }

    @WithMockUser(DEFAULT_LOGIN)
    @Test
    @Transactional
    public void downloadImage() throws Exception {
        String fileName = uploadImageSuccessfully();

        File imageTest = gardenFieldPath.resolve(fileName).toFile();
        assertThat(imageTest.exists()).isTrue();
        restFileMockMvc.perform(MockMvcRequestBuilders.get("/api/v1/gardenfields/" + gardenField.getId() + "/downloadImage/" + fileName))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk());
    }

    @WithMockUser(DEFAULT_LOGIN)
    @Test
    @Transactional
    public void getAllImageNames_WithoutImages() throws Exception {
        restFileMockMvc.perform(MockMvcRequestBuilders.get("/api/v1/gardenfields/" + gardenField.getId() + "/downloadImages"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @WithMockUser(DEFAULT_LOGIN)
    @Test
    @Transactional
    public void getAllImageNames_WithImages() throws Exception {
        String fileName = uploadImageSuccessfully();
        String fileName2 = uploadImageSuccessfully();

        restFileMockMvc.perform(MockMvcRequestBuilders.get("/api/v1/gardenfields/" + gardenField.getId() + "/downloadImages"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$").value(containsInAnyOrder(fileName, fileName2)));
    }

    @WithMockUser(DEFAULT_LOGIN)
    @Test
    @Transactional
    public void deleteImage() throws Exception {
        String fileName = uploadImageSuccessfully();

        File imageTest = gardenFieldPath.resolve(fileName).toFile();
        File thumbnailTest = gardenFieldPath.resolve("thumbnails").resolve(fileName).toFile();
        assertThat(imageTest.exists()).isTrue();
        assertThat(thumbnailTest.exists()).isTrue();
        restFileMockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/gardenfields/" + gardenField.getId() + "/" + fileName))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent());

        assertThat(imageTest.exists()).isFalse();
        assertThat(thumbnailTest.exists()).isFalse();
    }

    private String uploadImageSuccessfully() throws Exception {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("images/logo.png")).getFile());
        MockMultipartFile image = new MockMultipartFile("image", "logo.png", "image/png", FileUtils.readFileToByteArray(file));

        MvcResult mvcResult = restFileMockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/gardenfields/" + gardenField.getId() + "/uploadImage")
            .file(image))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.imageName").value(containsString(".png")))
            .andExpect(jsonPath("$.imageDownloadUri").value(containsString("/api/v1/gardenfields/" + gardenField.getId() + "/downloadImage/")))
            .andExpect(jsonPath("$.thumbnailDownloadUri").value(containsString("/api/v1/gardenfields/" + gardenField.getId() + "/downloadThumbnail/")))
            .andExpect(jsonPath("$.imageType").value(is("image/png")))
            .andReturn();

        String fileName = JsonPath.parse(mvcResult.getResponse().getContentAsString()).read("$.imageName").toString();
        assertThat(gardenFieldPath.resolve(fileName).toFile().exists()).isTrue();
        assertThat(gardenFieldPath.resolve("thumbnails").resolve(fileName).toFile().exists()).isTrue();
        return fileName;
    }
}
