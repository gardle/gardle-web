package com.gardle.service.dto.leasing;

import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.service.dto.MessageDTO;
import com.gardle.service.dto.SimpleGardenFieldDTO;
import com.gardle.service.dto.SimpleUserDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * A DTO for the {@link com.gardle.domain.Leasing} entity.
 */

@ApiModel(description = "All details about leasings")
@Data
public class LeasingDTO implements Serializable {

    @ApiModelProperty(value = "database id of the leasing", dataType = "Long")
    private Long id;

    @NotNull
    @ApiModelProperty(value = "start point of the leasing", dataType = "Instant")
    private Instant from;

    @NotNull
    @ApiModelProperty(value = "end point of the leasing", dataType = "Instant")
    private Instant to;

    @NotNull
    @ApiModelProperty(value = "status of the leasing", dataType = "LeasingStatus(OPEN,RESERVED,REJECTED,CANCELLED)")
    private LeasingStatus status;

    @NotNull
    @ApiModelProperty(value = "user of the leasing", dataType = "SimpleUserDTO")
    private SimpleUserDTO user;

    @NotNull
    private Set<MessageDTO> messages = new HashSet<>();

    @NotNull
    @ApiModelProperty(value = "the leased gardenfield", dataType = "SimpleGardenFieldDTO")
    private SimpleGardenFieldDTO gardenField;

    public LeasingDTO() {

    }

    @AssertTrue
    private boolean isFromBeforeTo() {
        return from.isBefore(to);
    }
}
