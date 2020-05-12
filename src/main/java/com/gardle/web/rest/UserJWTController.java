package com.gardle.web.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gardle.domain.User;
import com.gardle.security.jwt.JWTFilter;
import com.gardle.security.jwt.TokenProvider;
import com.gardle.service.UserService;
import com.gardle.web.rest.errors.GardleErrorKey;
import com.gardle.web.rest.errors.GardleRestControllerException;
import com.gardle.web.rest.errors.NotFoundException;
import com.gardle.web.rest.errors.UnauthorizedException;
import com.gardle.web.rest.vm.LoginVM;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.StatusType;

import javax.validation.Valid;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api/v1")
public class UserJWTController {

    private static class UserResourceException extends GardleRestControllerException {
        private UserResourceException(GardleErrorKey errorKey, StatusType statusType, String details) {
            super(errorKey, statusType, details);
        }
    }

    private final TokenProvider tokenProvider;

    private final UserService userService;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public UserJWTController(TokenProvider tokenProvider,
                             AuthenticationManagerBuilder authenticationManagerBuilder,
                             UserService userService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userService = userService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM) {

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(loginVM.getUsername(), loginVM.getPassword());
        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            throw new UnauthorizedException(GardleErrorKey.PASSWORD_INVALID, e.getMessage());
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        boolean rememberMe = (loginVM.isRememberMe() == null) ? false : loginVM.isRememberMe();
        String jwt = tokenProvider.createToken(authentication, rememberMe);

        User user = userService.getUserWithAuthorities().orElseThrow(() -> new NotFoundException(GardleErrorKey.USER_NOT_FOUND));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt, user.getId(), user.getFirstName(), user.getLastName()), httpHeaders, HttpStatus.OK);
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;
        private final Long id;
        private final String firstname;
        private final String lastname;

        JWTToken(String idToken, Long id, String firstname, String lastname) {
            this.idToken = idToken;
            this.id = id;
            this.firstname = firstname;
            this.lastname = lastname;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        @JsonProperty("id")
        Long getId() {
            return id;
        }

        @JsonProperty("firstname")
        String getFirstname() {
            return firstname;
        }

        @JsonProperty("lastname")
        String getLastname() {
            return lastname;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
