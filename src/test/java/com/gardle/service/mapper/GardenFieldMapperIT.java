package com.gardle.service.mapper;

import com.gardle.GardleApp;
import com.gardle.domain.GardenField;
import com.gardle.domain.User;
import com.gardle.repository.UserRepository;
import com.gardle.service.dto.GardenFieldDTO;
import com.gardle.service.dto.SimpleUserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = GardleApp.class)
@ExtendWith(SpringExtension.class)
public class GardenFieldMapperIT {

    private static final Long USER_ID = 1L;
    private static final Long GARDEN_ID = 1L;
    private static final String FIRST_NAME = "testFirstName";
    private static final String LAST_NAME = "testLastName";
    private static final String GARDEN_NAME = "testGarden";
    private static final String DESCRIPTION = "testDescription";
    private static final Double SIZE_IN_M2 = 10.0;
    private static final Double PRICE_PER_M2 = 3.0;
    private static final Double LATITUDE = 1.0;
    private static final Double LONGITUDE = 1.0;
    private static final String CITY = "testCity";
    private static final Boolean ROOFED = false;
    private static final Boolean GLASS_HOUSE = false;
    private static final Boolean HIGH = true;
    private static final Boolean WATER = true;
    private static final Boolean ELECTRICITY = false;
    private static final Double PH_VALUE = null;
    @Autowired
    private GardenFieldMapper gardenFieldMapper;
    @Autowired
    private SimpleUserMapper simpleUserMapper;
    private User user;
    private SimpleUserDTO userDTO;
    private GardenFieldDTO gardenFieldDTO;
    private GardenField gardenField;
    @MockBean
    private UserRepository userRepository;


    @BeforeEach
    public void init() {
        user = new User();
        user.setId(USER_ID);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        userDTO = simpleUserMapper.toDTO(user);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        gardenFieldDTO = new GardenFieldDTO();
        gardenFieldDTO.setId(GARDEN_ID);
        gardenFieldDTO.setName(GARDEN_NAME);
        gardenFieldDTO.setDescription(DESCRIPTION);
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
        gardenFieldDTO.setOwner(userDTO);

        gardenField = new GardenField();
        gardenField.setId(GARDEN_ID);
        gardenField.setName(GARDEN_NAME);
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
    }

    @Test
    public void map_shouldMapGardenFieldDTOToGardenField() {
        GardenField gardenFieldEntity = gardenFieldMapper.toEntity(gardenFieldDTO);
        assertThat(gardenFieldEntity).isNotNull();
        assertThat(gardenFieldEntity.getId()).isEqualTo(GARDEN_ID);
        assertThat(gardenFieldEntity.getName()).isEqualTo(GARDEN_NAME);
        assertThat(gardenFieldEntity.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(gardenFieldEntity.getSizeInM2()).isEqualTo(SIZE_IN_M2);
        assertThat(gardenFieldEntity.getPricePerM2()).isEqualTo(PRICE_PER_M2);
        assertThat(gardenFieldEntity.getLatitude()).isEqualTo(LATITUDE);
        assertThat(gardenFieldEntity.getLongitude()).isEqualTo(LONGITUDE);
        assertThat(gardenFieldEntity.getCity()).isEqualTo(CITY);
        assertThat(gardenFieldEntity.getRoofed()).isEqualTo(ROOFED);
        assertThat(gardenFieldEntity.getGlassHouse()).isEqualTo(GLASS_HOUSE);
        assertThat(gardenFieldEntity.getHigh()).isEqualTo(HIGH);
        assertThat(gardenFieldEntity.getWater()).isEqualTo(WATER);
        assertThat(gardenFieldEntity.getElectricity()).isEqualTo(ELECTRICITY);
        assertThat(gardenFieldEntity.getPhValue()).isEqualTo(PH_VALUE);
        assertThat(gardenFieldEntity.getOwner()).isEqualTo(user);
    }

    @Test
    public void map_shouldMapGardenFieldToGardenFieldDTO() {
        GardenFieldDTO gardenFieldDto = gardenFieldMapper.toDto(gardenField);
        assertThat(gardenFieldDto).isNotNull();
        assertThat(gardenFieldDto.getId()).isEqualTo(GARDEN_ID);
        assertThat(gardenFieldDto.getName()).isEqualTo(GARDEN_NAME);
        assertThat(gardenFieldDto.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(gardenFieldDto.getSizeInM2()).isEqualTo(SIZE_IN_M2);
        assertThat(gardenFieldDto.getPricePerM2()).isEqualTo(PRICE_PER_M2);
        assertThat(gardenFieldDto.getLatitude()).isEqualTo(LATITUDE);
        assertThat(gardenFieldDto.getLongitude()).isEqualTo(LONGITUDE);
        assertThat(gardenFieldDto.getCity()).isEqualTo(CITY);
        assertThat(gardenFieldDto.getRoofed()).isEqualTo(ROOFED);
        assertThat(gardenFieldDto.getGlassHouse()).isEqualTo(GLASS_HOUSE);
        assertThat(gardenFieldDto.getHigh()).isEqualTo(HIGH);
        assertThat(gardenFieldDto.getWater()).isEqualTo(WATER);
        assertThat(gardenFieldDto.getElectricity()).isEqualTo(ELECTRICITY);
        assertThat(gardenFieldDto.getPhValue()).isEqualTo(PH_VALUE);
        assertThat(gardenFieldDto.getOwner()).isEqualTo(userDTO);
    }
}
