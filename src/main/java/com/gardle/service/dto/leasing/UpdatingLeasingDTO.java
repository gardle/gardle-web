package com.gardle.service.dto.leasing;

import com.gardle.domain.enumeration.LeasingStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(description = "All information of the leasing needed for updating")
@Data
public class UpdatingLeasingDTO implements Serializable {

    @NotNull
    @ApiModelProperty(value = "database id of the leasing", dataType = "Long")
    private Long id;

    @NotNull
    @ApiModelProperty(value = "the id of the leased gardenfield", dataType = "SimpleGardenFieldDTO")
    private Long gardenFieldId;

    @NotNull
    @ApiModelProperty(value = "status of the leasing", dataType = "LeasingStatus(OPEN,RESERVED,REJECTED,CANCELLED)")
    private LeasingStatus status;
}
