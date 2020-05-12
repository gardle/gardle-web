package com.gardle.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;

@Data
@NoArgsConstructor
@ApiModel(description = "All details about filter boundaries")
public class FilterBoundariesDTO implements Serializable {

    @ApiModelProperty(value = "The maximum of gardenfield price", dataType = "Double")
    @Positive
    private Double maxPrice;

    @ApiModelProperty(value = "The minimum of gardenfield price", dataType = "Double")
    @PositiveOrZero
    private Double minPrice;

    @ApiModelProperty(value = "The maximum of gardenfield size", dataType = "Double")
    @Positive
    private Double maxSize;

    @ApiModelProperty(value = "The minimum of gardenfield size", dataType = "Double")
    @Positive
    private Double minSize;

}
