package com.gardle.service.dto;

import com.gardle.config.Constants;
import com.gardle.domain.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@ApiModel(description = "Simple information about the user")
public class SimpleUserDTO {

    @ApiModelProperty(value = "The database generated user ID", dataType = "Long")
    @NotNull
    private Long id;

    @ApiModelProperty(value = "The username (login) of the user", required = true, dataType = "String")
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    @ApiModelProperty(value = "The first name of the user", required = true, dataType = "String")
    @Size(max = 50)
    private String firstName;

    @ApiModelProperty(value = "The last name of the user", required = true, dataType = "String")
    @Size(max = 50)
    private String lastName;

    @ApiModelProperty(value = "The email of the user", required = true, dataType = "String")
    @Email
    @Size(min = 5, max = 254)
    private String email;

    public SimpleUserDTO(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
    }
}
