package com.gardle.domain.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class GardenFieldFilterCriteria {
    private LocationFilterCriteria locationFilterCriteria;
    private Double minPricePerM2;
    private Double maxPricePerM2;
    private Double sizeInM2LowerBound;
    private Double sizeInM2UpperBound;
    private Boolean roofed;
    private Instant leasingTimeFrom;
    private Instant leasingTimeTo;
    private String keywords;
    private Boolean water;
    private Boolean electricity;
    private Boolean high;
    private Boolean glassHouse;
    //if element is added -> also add in checkAllNull!

    public boolean checkAllNull() {
        return (locationFilterCriteria == null || locationFilterCriteria.checkAllNull()) &&
            minPricePerM2 == null && maxPricePerM2 == null &&
            sizeInM2LowerBound == null &&
            sizeInM2UpperBound == null && roofed == null &&
            leasingTimeFrom == null && leasingTimeTo == null &&
            keywords == null && electricity == null && water == null &&
            high == null && glassHouse == null;
    }
}
