package com.gardle.web.rest;


import com.gardle.domain.User;
import com.gardle.repository.UserRepository;
import com.gardle.security.SecurityUtils;
import com.gardle.service.MailService;
import com.gardle.service.UserService;
import com.gardle.service.dto.PasswordChangeDTO;
import com.gardle.service.dto.UserDTO;
import com.gardle.web.rest.errors.BadRequestException;
import com.gardle.web.rest.errors.ConflictException;
import com.gardle.web.rest.errors.GardleErrorKey;
import com.gardle.web.rest.errors.GardleRestControllerException;
import com.gardle.web.rest.vm.KeyAndPasswordVM;
import com.gardle.web.rest.vm.ManagedUserVM;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.Status;
import org.zalando.problem.StatusType;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api/v1")
public class AccountController {

    private static class AccountResourceException extends GardleRestControllerException {
        private AccountResourceException(GardleErrorKey errorKey, StatusType statusType, String details) {
            super(errorKey, statusType, details);
        }
    }

    private final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;

    public AccountController(UserRepository userRepository, UserService userService, MailService mailService) {

        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
    }

    private static boolean checkForInvalidPasswordLength(String password) {
        return StringUtils.isEmpty(password) ||
            password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
            password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH;
    }

    /**
     * {@code GET  /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws AccountResourceException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */
    @GetMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new AccountResourceException(GardleErrorKey.NO_USER_FOUND_FOR_ACTIVATION_KEY, Status.NOT_FOUND, "User not found for activation key");
        }
    }

    /**
     * {@code GET  /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @return the current user.
     * @throws AccountResourceException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @GetMapping("/account")
    public UserDTO getAccount() {
        return userService.getUserWithAuthorities()
            .map(UserDTO::new)
            .orElseThrow(() -> new AccountResourceException(GardleErrorKey.USER_NOT_FOUND, Status.NOT_FOUND, "User not found"));
    }

    /**
     * {@code POST  /account} : update the current user information.
     *
     * @param userDTO the current user information.
     * @throws AccountResourceException {@code 404 (Internal Server Error)} if the user login wasn't found.
     * @throws ConflictException        {@code 409 (Conflict)} if the email is already used or if another user has the same ID
     */
    @PostMapping("/account")
    public void saveAccount(@Valid @RequestBody UserDTO userDTO) throws ConflictException, AccountResourceException {
        String userLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new AccountResourceException(GardleErrorKey.CURRENT_LOGIN_NOT_FOUND, Status.NOT_FOUND, "Username not found"));
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new AccountResourceException(GardleErrorKey.USER_NOT_FOUND, Status.NOT_FOUND, "User not found");
        }

        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        // Check if there is a different user with the same email
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.get().getId())) {
            throw new ConflictException(GardleErrorKey.EMAIL_ALREADY_EXISTS);
        }

        // Changing login currently appears to lead to issues with caching -> a phantom user remains active in the application
        // Hence it is disabled in the frontend
        Optional<User> existingUserByLogin = userRepository.findOneByLogin(userDTO.getLogin());
        // Check if there is a different user with the same login
        if (existingUserByLogin.isPresent() && !existingUserByLogin.get().getId().equals(user.get().getId())) {
            throw new ConflictException(GardleErrorKey.LOGIN_ALREADY_EXISTS);
        }

        userService.updateCurrentUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(),
            userDTO.getLangKey(), userDTO.getBirthDate(), userDTO.getTel(), userDTO.getLogin());
    }

    /**
     * {@code POST  /register} : register the user.
     *
     * @param managedUserVM the managed user View Model.
     * @throws BadRequestException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws BadRequestException {@code 400 (Bad Request)} if the email is already used.
     * @throws BadRequestException {@code 400 (Bad Request)} if the login is already used.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) throws BadRequestException {
        if (checkForInvalidPasswordLength(managedUserVM.getPassword())) {
            throw new BadRequestException(GardleErrorKey.PASSWORD_INVALID_LENGTH);
        }
        User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
        mailService.sendActivationEmail(user);
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     * @throws BadRequestException {@code 400 (Bad Request)} if the email address is not registered.
     */
    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody String mail) throws BadRequestException {
        mailService.sendPasswordResetMail(
            userService.requestPasswordReset(mail)
                .orElseThrow(() -> new BadRequestException(GardleErrorKey.EMAIL_NOT_FOUND))
        );
    }

    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws BadRequestException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) throws BadRequestException {
        if (checkForInvalidPasswordLength(passwordChangeDto.getNewPassword())) {
            throw new BadRequestException(GardleErrorKey.PASSWORD_INVALID_LENGTH);
        }
        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws BadRequestException      {@code 400 (Bad Request)} if the password is incorrect.
     * @throws AccountResourceException {@code 404 (Internal Server Error)} if user could not be found
     */
    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) throws AccountResourceException, BadRequestException {
        if (checkForInvalidPasswordLength(keyAndPassword.getNewPassword())) {
            throw new BadRequestException(GardleErrorKey.PASSWORD_INVALID_LENGTH);
        }
        Optional<User> user =
            userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            throw new AccountResourceException(GardleErrorKey.USER_FOR_RESET_KEY_NOT_FOUND, Status.NOT_FOUND, "Reset key not found");
        }
    }
}
