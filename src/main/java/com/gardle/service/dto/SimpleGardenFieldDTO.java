package com.gardle.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@NoArgsConstructor
@ApiModel(description = "Simple representation of a gardenfield")
public class SimpleGardenFieldDTO implements Serializable {

    @ApiModelProperty(value = "The database generated gardenfield ID", dataType = "Long")
    private Long id;

    @ApiModelProperty(value = "The name of the gardenfield", required = true, dataType = "String")
    @NotBlank
    private String name;
}
