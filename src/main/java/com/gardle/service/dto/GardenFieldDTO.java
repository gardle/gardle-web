package com.gardle.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@ApiModel(description = "All details about the Gardenfield.")
public class GardenFieldDTO implements Serializable {

    @ApiModelProperty(value = "The database generated gardenfield ID", dataType = "Long")
    private Long id;

    @ApiModelProperty(value = "The name of the gardenfield", required = true, dataType = "String")
    @Size(min = 3, max = 100)
    @SafeHtml
    @NotBlank
    private String name;

    @ApiModelProperty(value = "The description of the gardenfield", dataType = "String")
    @Size(max = 1000)
    @SafeHtml
    private String description;

    @ApiModelProperty(value = "The field size in square meters (>0)", required = true, dataType = "Double")
    @Positive
    @NotNull
    private Double sizeInM2;

    @ApiModelProperty(value = "The price per square meters (>0 and in euros)", required = true, dataType = "Double")
    @PositiveOrZero
    @NotNull
    private Double pricePerM2;

    @ApiModelProperty(value = "The latitude of the gardenfield", required = true, dataType = "Double")
    @NotNull
    private Double latitude;

    @ApiModelProperty(value = "The longitude of the gardenfield", required = true, dataType = "Double")
    @NotNull
    private Double longitude;

    @ApiModelProperty(value = "The city where the gardenfield is located", required = true)
    @NotNull
    private String city;

    @ApiModelProperty(value = "Defines if the gardenfield is roofed or not", dataType = "Boolean")
    private Boolean roofed;

    @ApiModelProperty(value = "Defines if the gardenfield is in a glasshouse or not", dataType = "Boolean")
    private Boolean glassHouse;

    @ApiModelProperty(value = "Defines if the gardenfield is raised (high) or not", dataType = "Boolean")
    private Boolean high;

    @ApiModelProperty(value = "Defines if water is available", dataType = "Boolean")
    private Boolean water;

    @ApiModelProperty(value = "Defines if electricity is available", dataType = "Boolean")
    private Boolean electricity;

    @ApiModelProperty(value = "The ph-value of the gardenfield", dataType = "Boolean")
    @PositiveOrZero
    private Double phValue;

    @ApiModelProperty(value = "Simple User of the owner for the gardenfield")
    @Valid
    private SimpleUserDTO owner;

    @ApiModelProperty(value = "cover image name")
    private String coverImage;

    @ApiModelProperty(value = "price per month == pricePerM2 * sizeInM2 * 30", dataType = "Double")
    private Double pricePerMonth;
}
