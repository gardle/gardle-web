package com.gardle.service.validator;

import com.gardle.GardleApp;
import com.gardle.domain.criteria.GardenFieldFilterCriteria;
import com.gardle.domain.criteria.LocationFilterCriteria;
import com.gardle.validator.GardenFieldFilterCriteriaValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = GardleApp.class)
@ExtendWith(SpringExtension.class)
public class GardenfieldFilterCriteraValidatorTest {

    private static final Double PRICE_PER_M2 = 3.0;
    private static final Double LATITUDE = 1.0;
    private static final Double LONGITUDE = 1.0;
    private static final Instant LEASING_TIME_FROM = Instant.parse("2020-01-10T18:00:00.00Z");
    private static final Instant LEASING_TIME_TO = Instant.parse("2020-10-10T18:00:00.00Z");
    private final GardenFieldFilterCriteriaValidator gardenFieldFilterCriteriaValidator = new GardenFieldFilterCriteriaValidator();

    @Test
    public void latWithoutLon_shouldReturnFalse() {
        GardenFieldFilterCriteria gardenFieldFilterCriteria = new GardenFieldFilterCriteria(
            new LocationFilterCriteria(LATITUDE, null, null), null, null, null, null, null,
            null, null, null, null, null, null, null);
        assertThat(gardenFieldFilterCriteriaValidator.isValid(gardenFieldFilterCriteria)).isNotEmpty();
    }

    @Test
    public void longWithoutLat_shouldReturnFalse() {
        GardenFieldFilterCriteria gardenFieldFilterCriteria = new GardenFieldFilterCriteria(
            new LocationFilterCriteria(null, LONGITUDE, null), null, null, null, null, null,
            null, null, null, null, null, null, null);
        assertThat(gardenFieldFilterCriteriaValidator.isValid(gardenFieldFilterCriteria)).isNotEmpty();
    }

    @Test
    public void maxPriceNegative_shouldReturnFalse() {
        GardenFieldFilterCriteria gardenFieldFilterCriteria = new GardenFieldFilterCriteria(
            new LocationFilterCriteria(null, null, null), null, -10.0, null, null,
            null, null, null, null, null, null, null, null);
        assertThat(gardenFieldFilterCriteriaValidator.isValid(gardenFieldFilterCriteria)).isNotEmpty();
    }

    @Test
    public void minPriceNegative_shouldReturnFalse() {
        GardenFieldFilterCriteria gardenFieldFilterCriteria = new GardenFieldFilterCriteria(
            new LocationFilterCriteria(null, null, null), -10.0, null, null, null,
            null, null, null, null, null, null, null, null);
        assertThat(gardenFieldFilterCriteriaValidator.isValid(gardenFieldFilterCriteria)).isNotEmpty();
    }

    @Test
    public void sizeUpperBoundLowerThanSizeLowerBound_shouldReturnFalse() {
        GardenFieldFilterCriteria gardenFieldFilterCriteria = new GardenFieldFilterCriteria(
            new LocationFilterCriteria(null, null, null), null, null, 10.0, 9.0, null,
            null, null, null, null, null, null, null);
        assertThat(gardenFieldFilterCriteriaValidator.isValid(gardenFieldFilterCriteria)).isNotEmpty();
    }

    @Test
    public void leasingTimeFromAfterLeasingTimeTo_shouldReturnFalse() {
        GardenFieldFilterCriteria gardenFieldFilterCriteria = new GardenFieldFilterCriteria(
            new LocationFilterCriteria(null, null, null), null, null, 10.0, null, null,
            LEASING_TIME_TO, LEASING_TIME_FROM, null, null, null, null, null);
        assertThat(gardenFieldFilterCriteriaValidator.isValid(gardenFieldFilterCriteria)).isNotEmpty();
    }

    @Test
    public void allNull_shouldReturnTrue() {
        GardenFieldFilterCriteria gardenFieldFilterCriteria = new GardenFieldFilterCriteria(
            new LocationFilterCriteria(null, null, null), null, null, 10.0, null,
            null, null, null, null, null, null, null, null);
        assertThat(gardenFieldFilterCriteriaValidator.isValid(gardenFieldFilterCriteria)).isEmpty();
    }

    @Test
    public void randomValues_shouldReturnTrue() {
        GardenFieldFilterCriteria gardenFieldFilterCriteria = new GardenFieldFilterCriteria(
            new LocationFilterCriteria(LATITUDE, LONGITUDE, 1), null, PRICE_PER_M2, 10.0, 15.0, false,
            LEASING_TIME_FROM, LEASING_TIME_TO, null, null, null, true, null);
        assertThat(gardenFieldFilterCriteriaValidator.isValid(gardenFieldFilterCriteria)).isEmpty();
    }
}
