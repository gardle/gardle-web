package com.gardle.validator;

import com.gardle.domain.criteria.GardenFieldFilterCriteria;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

/**
 * custom constraint validator checking a {@link GardenFieldFilterCriteriaValidator}
 * Rules:
 * 1. Latitude, Longitude, Radius must be set with all or nothing principle
 * 2. Radius, Size, Price must not be smaller than 0
 * 3. SizeLowerbound must be smaller than SizeUpperBound
 * 4. LeasingTimeFrom must be before LeasingTimeTo
 */
@Service
public class GardenFieldFilterCriteriaValidator {

    public String isValid(@Nullable final GardenFieldFilterCriteria gardenFieldFilterCriteria) {
        String errorMsg = "";
        //valid if null
        if (gardenFieldFilterCriteria == null) {
            return errorMsg;
        }

        if (gardenFieldFilterCriteria.getLocationFilterCriteria() != null &&
            !gardenFieldFilterCriteria.getLocationFilterCriteria().checkNonNull() &&
            !gardenFieldFilterCriteria.getLocationFilterCriteria().checkAllNull()) {
            errorMsg += "invalid location (latitude, longitude, radius) filter;";
        }

        if (gardenFieldFilterCriteria.getLocationFilterCriteria() != null
            && gardenFieldFilterCriteria.getLocationFilterCriteria().checkNonNull()
            && gardenFieldFilterCriteria.getLocationFilterCriteria().getRadiusInKM() < 0) {
            errorMsg += "invalid radius filter;";
        }

        if (gardenFieldFilterCriteria.getMinPricePerM2() != null && gardenFieldFilterCriteria.getMinPricePerM2() < 0) {
            errorMsg += "invalid price filter criteria;";
        }

        if (gardenFieldFilterCriteria.getMaxPricePerM2() != null && gardenFieldFilterCriteria.getMaxPricePerM2() < 0) {
            errorMsg += "invalid price filter criteria;";
        }

        if (gardenFieldFilterCriteria.getSizeInM2LowerBound() != null && gardenFieldFilterCriteria.getSizeInM2UpperBound() != null) {
            if (gardenFieldFilterCriteria.getSizeInM2LowerBound() < 0 || gardenFieldFilterCriteria.getSizeInM2UpperBound() < 0 ||
                gardenFieldFilterCriteria.getSizeInM2LowerBound() > gardenFieldFilterCriteria.getSizeInM2UpperBound()) {
                errorMsg += "invalid size filter criteria;";
            }
        }

        if (gardenFieldFilterCriteria.getLeasingTimeFrom() != null && gardenFieldFilterCriteria.getLeasingTimeTo() != null) {
            if (gardenFieldFilterCriteria.getLeasingTimeFrom().isAfter(gardenFieldFilterCriteria.getLeasingTimeTo())) {
                errorMsg += "invalid leasing time filter criteria;";
            }
        }
        return errorMsg;
    }
}
