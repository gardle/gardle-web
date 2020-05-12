package com.gardle.service.dto;

import com.gardle.domain.Leasing;
import com.gardle.domain.enumeration.MessageType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * A DTO for the {@link com.gardle.domain.Message} entity.
 */
@Data
@NoArgsConstructor
@ApiModel(description = "All details about the messages.")
public class MessageDTO implements Serializable {

    @ApiModelProperty(value = "The database generated message ID", dataType = "Long")
    private Long id;

    @ApiModelProperty(value = "The content of the message", required = true, dataType = "String")
    @NotBlank
    @Size(max = 2000)
    @SafeHtml
    private String content;

    @ApiModelProperty(value = "The type of the message", dataType = "MessageType")
    private MessageType type;

    @ApiModelProperty(value = "The sender of the message")
    @NotNull
    @Valid
    private SimpleUserDTO userFrom;

    @ApiModelProperty(value = "The receiver of the message")
    @NotNull
    @Valid
    private SimpleUserDTO userTo;

    @ApiModelProperty(value = "The thread from the message", dataType = "UUID")
    private UUID thread;

    @ApiModelProperty(value = "The created timestamp from the message", dataType = "Instant")
    private Instant createdDate;

    @ApiModelProperty(value = "Signifies if the message has been opened", dataType = "Boolean")
    private Boolean opened;

    @ApiModelProperty(value = "References a leasing")
    private Leasing leasing;
}
