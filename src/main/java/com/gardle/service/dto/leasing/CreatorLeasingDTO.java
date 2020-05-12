package com.gardle.service.dto.leasing;

import com.gardle.validator.ValidLeasingData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

@ApiModel(description = "All information of the leasing needed for creating")
@Data
@ValidLeasingData
public class CreatorLeasingDTO implements Serializable {

    @NotNull
    @ApiModelProperty(value = "start point of the leasing", dataType = "Instant")
    private Instant from;

    @NotNull
    @ApiModelProperty(value = "end point of the leasing", dataType = "Instant")
    private Instant to;

    @NotNull
    @ApiModelProperty(value = "the id of the leased gardenfield", dataType = "Long")
    private Long gardenFieldId;

    @NotNull
    @NotEmpty
    private String gardenFieldName;

    @ApiModelProperty(value = "the id of the requester", dataType = "Long")
    private Long userId;

    @AssertTrue
    private boolean isFromBeforeTo() {
        return from.isBefore(to);
    }

}
