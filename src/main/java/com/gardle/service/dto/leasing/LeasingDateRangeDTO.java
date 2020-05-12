package com.gardle.service.dto.leasing;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "All details about the Gardenfield.")
public class LeasingDateRangeDTO implements Serializable {

    @NotNull
    @ApiModelProperty(value = "start point of date range", dataType = "Instant")
    private Instant from;

    @NotNull
    @ApiModelProperty(value = "end point of date range", dataType = "Instant")
    private Instant to;
}
