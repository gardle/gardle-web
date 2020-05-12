package com.gardle.web.rest.errors;

import com.gardle.service.exception.*;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;
import org.zalando.problem.violations.ConstraintViolationProblem;

import javax.annotation.Nullable;

/**
 * Controller advice to translate the server side exceptions to exceptions that contain error codes for the client
 */
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling, SecurityAdviceTrait {
    @Override
    public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity, NativeWebRequest request) {
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Problem problem = entity.getBody();
        if (!(problem instanceof ConstraintViolationProblem)) {
            return entity;
        }
        ProblemBuilder builder = Problem.builder()
            .withStatus(problem.getStatus())
            .withTitle(GardleErrorKey.VALIDATION_ERROR.name())
            .with("Violations", ((ConstraintViolationProblem) problem).getViolations());

        return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
    }


    @ExceptionHandler
    public ResponseEntity<Problem> handleAccessDenied(AccessDeniedException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.FORBIDDEN)
            .withTitle(GardleErrorKey.ACCESS_DENIED.name())
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleConcurrencyFailure(ConcurrencyFailureException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.CONFLICT)
            .build();
        return create(ex, problem, request);
    }

    // Bad Request - 400

    @ExceptionHandler(PaymentNotSetServiceException.class)
    public ResponseEntity<Problem> paymentProviderServiceException(PaymentNotSetServiceException ex, NativeWebRequest request) {
        return create(new BadRequestException(GardleErrorKey.PAYMENT_OF_LEASING_NOT_SET, ex.getMessage()), request);
    }

    @ExceptionHandler(BankAccountCreationEmailEmptyServiceException.class)
    public ResponseEntity<Problem> bankAccountIbanEmptyException(BankAccountCreationEmailEmptyServiceException ex, NativeWebRequest request) {
        return create(new BadRequestException(GardleErrorKey.MISSING_BANK_ACCOUNT_EMAIL, ex.getMessage()), request);
    }

    @ExceptionHandler(BankAccountCreationIbanEmptyServiceException.class)
    public ResponseEntity<Problem> bankAccountIbanEmptyException(BankAccountCreationIbanEmptyServiceException ex, NativeWebRequest request) {
        return create(new BadRequestException(GardleErrorKey.MISSING_BANK_ACCOUNT_IBAN, ex.getMessage()), request);
    }

    @ExceptionHandler(GardenFieldFilterCriteriaValidationServiceException.class)
    public ResponseEntity<Problem> gardenFieldFilterCriteriaValidationException(GardenFieldFilterCriteriaValidationServiceException ex, NativeWebRequest request) {
        return create(new BadRequestException(GardleErrorKey.GARDENFIELD_FILTER_CRITERIA_VALIDATION, ex.getMessage()), request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleInvalidPasswordException(InvalidPasswordServiceException ex, NativeWebRequest request) {
        return create(new BadRequestException(GardleErrorKey.PASSWORD_INVALID, ex.getMessage()), request);
    }

    @ExceptionHandler(GardenFieldWithoutOwnerServiceException.class)
    public ResponseEntity<Problem> handleGardenFieldWithoutOwnerException(GardenFieldWithoutOwnerServiceException ex, NativeWebRequest request) {
        return create(new BadRequestException(GardleErrorKey.GARDENFIELD_WITHOUT_OWNER, ex.getMessage()), request);
    }

    @ExceptionHandler(NotAnImageServiceException.class)
    public ResponseEntity<Problem> handleNotAnImageException(NotAnImageServiceException ex, NativeWebRequest request) {
        return create(new BadRequestException(GardleErrorKey.NOT_AN_IMAGE, ex.getMessage()), request);
    }

    @ExceptionHandler(ImageStorageServiceDeletionException.class)
    public ResponseEntity<Problem> handleFileDeletionError(ImageStorageServiceDeletionException ex, NativeWebRequest request) {
        return create(new BadRequestException(GardleErrorKey.COULD_NOT_DELETE_IMAGE, ex.getMessage()), request);
    }

    @ExceptionHandler(ImageStorageServiceException.class)
    public ResponseEntity<Problem> handleFileStorageException(ImageStorageServiceException ex, NativeWebRequest request) {
        return create(new BadRequestException(GardleErrorKey.IMAGE_STORAGE, ex.getMessage()), request);
    }

    // Unauthorized - 401

    @ExceptionHandler(MissingAuthorityForGardenFieldServiceException.class)
    public ResponseEntity<Problem> handleMissingAuthorityForGfException(MissingAuthorityForGardenFieldServiceException ex, NativeWebRequest request) {
        return create(new UnauthorizedException(GardleErrorKey.USER_NOT_AUTHORIZED_FOR_REQUESTED_GARDENFIELD, ex.getMessage()), request);
    }

    @ExceptionHandler(NotLoggedInServiceException.class)
    public ResponseEntity<Problem> handleMissingAuthorityForMessageException(NotLoggedInServiceException ex, NativeWebRequest request) {
        return create(new UnauthorizedException(GardleErrorKey.USER_NOT_LOGGED_IN, ex.getMessage()), request);
    }

    @ExceptionHandler(MissingAuthorityServiceException.class)
    public ResponseEntity<Problem> handleMissingAuthorityException(MissingAuthorityServiceException ex, NativeWebRequest request) {
        return create(new UnauthorizedException(GardleErrorKey.MISSING_AUTHORITY, ex.getMessage()), request);
    }

    //Forbidden - 403

    @ExceptionHandler(MissingAuthorityForMessageServiceException.class)
    public ResponseEntity<Problem> handleMissingAuthorityForMessageException(MissingAuthorityForMessageServiceException ex, NativeWebRequest request) {
        return create(new ForbiddenException(GardleErrorKey.USER_NOT_AUTHORIZED_FOR_REQUESTED_MESSAGES, ex.getMessage()), request);
    }

    @ExceptionHandler(MissingAuthorityForMessageThreadServiceException.class)
    public ResponseEntity<Problem> handleMissingAuthorityForMessageThreadException(MissingAuthorityForMessageThreadServiceException ex, NativeWebRequest request) {
        return create(new ForbiddenException(GardleErrorKey.USER_NOT_AUTHORIZED_FOR_REQUESTED_MESSAGE_THREAD, ex.getMessage()), request);
    }

    @ExceptionHandler(MissingPermissionServiceException.class)
    public ResponseEntity<Problem> handleMissingPermissionException(MissingPermissionServiceException ex, NativeWebRequest request) {
        return create(new ForbiddenException(GardleErrorKey.MISSING_PERMISSION, ex.getMessage()), request);
    }

    @ExceptionHandler(StripeVerificationKeyValidationServiceException.class)
    public ResponseEntity<Problem> handleStripeVerificationKeyValidationError(StripeVerificationKeyValidationServiceException ex, NativeWebRequest request) {
        return create(new ForbiddenException(GardleErrorKey.STRIPE_VERIFICATION_KEY_VALIDATION_ERROR, ex.getMessage()), request);
    }

    @ExceptionHandler(MissingStripeVerificationServiceException.class)
    public ResponseEntity<Problem> missingStripeVerificationException(MissingStripeVerificationServiceException ex, NativeWebRequest request) {
        return create(new ForbiddenException(GardleErrorKey.MISSING_STRIPE_VERIFICATION, ex.getMessage()), request);
    }

    //NotFound - 404

    @ExceptionHandler(GardenFieldNotFoundServiceException.class)
    public ResponseEntity<Problem> handleGardenFieldNotFoundException(GardenFieldNotFoundServiceException ex, NativeWebRequest request) {
        return create(new NotFoundException(GardleErrorKey.GARDENFIELD_NOT_FOUND, ex.getMessage()), request);
    }

    @ExceptionHandler(CoverNotFoundServiceException.class)
    public ResponseEntity<Problem> handleCoverFileNotFound(CoverNotFoundServiceException ex, NativeWebRequest request) {
        return create(new NotFoundException(GardleErrorKey.COVER_NOT_FOUND, ex.getMessage()), request);
    }

    @ExceptionHandler(ImageNotFoundServiceException.class)
    public ResponseEntity<Problem> handleFileNotFound(ImageNotFoundServiceException ex, NativeWebRequest request) {
        return create(new NotFoundException(GardleErrorKey.IMAGE_NOT_FOUND, ex.getMessage()), request);
    }

    @ExceptionHandler(LeasingNotFoundServiceException.class)
    public ResponseEntity<Problem> handleLeasingNotFoundException(LeasingNotFoundServiceException ex, NativeWebRequest request) {
        return create(new NotFoundException(GardleErrorKey.LEASING_NOT_FOUND, ex.getMessage()), request);
    }

    // Conflict - 409

    @ExceptionHandler(GardenFieldUnknownServiceException.class)
    public ResponseEntity<Problem> handleGardenFieldNotExistingException(GardenFieldUnknownServiceException ex, NativeWebRequest request) {
        return create(new ConflictException(GardleErrorKey.GARDENFIELD_NOT_FOUND, ex.getMessage()), request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleEmailAlreadyUsedException(EmailAlreadyUsedServiceException ex, NativeWebRequest request) {
        return create(new ConflictException(GardleErrorKey.EMAIL_ALREADY_EXISTS, ex.getMessage()), request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleUsernameAlreadyUsedException(UsernameAlreadyUsedServiceException ex, NativeWebRequest request) {
        return create(new ConflictException(GardleErrorKey.LOGIN_ALREADY_EXISTS, ex.getMessage()), request);
    }

    @ExceptionHandler(LeasingsOverlapServiceException.class)
    public ResponseEntity<Problem> handleLeasingsOverlapException(LeasingsOverlapServiceException ex, NativeWebRequest request) {
        return create(new ConflictException(GardleErrorKey.LEASINGS_OVERLAP, ex.getMessage()), request);
    }

    @ExceptionHandler(InvalidLeasingStateServiceException.class)
    public ResponseEntity<Problem> handleInvalidLeasingStateException(InvalidLeasingStateServiceException ex, NativeWebRequest request) {
        return create(new ConflictException(GardleErrorKey.LEASING_STATUS_TRANSITION_NOT_ALLOWED, ex.getMessage()), request);
    }

    @ExceptionHandler(LeasingUpdateNotAllowedServiceException.class)
    public ResponseEntity<Problem> handleLeasingUpdateNotAllowedException(LeasingUpdateNotAllowedServiceException ex, NativeWebRequest request) {
        return create(new ConflictException(GardleErrorKey.LEASING_UPDATE_NOT_ALLOWED_IN_PERIOD, ex.getMessage()), request);
    }

    @ExceptionHandler(LeasingCreateNotAllowedServiceException.class)
    public ResponseEntity<Problem> handleLeasingCreateNotAllowedException(LeasingCreateNotAllowedServiceException ex, NativeWebRequest request) {
        return create(new ConflictException(GardleErrorKey.LEASING_CREATE_NOT_ALLOWED_IN_PERIOD, ex.getMessage()), request);
    }

    @ExceptionHandler(LeasingTooShortServiceException.class)
    public ResponseEntity<Problem> handleLeasingTooShortException(LeasingTooShortServiceException ex, NativeWebRequest request) {
        return create(new ConflictException(GardleErrorKey.LEASING_TOO_SHORT, ex.getMessage()), request);
    }

    @ExceptionHandler(UserForLeasingDoesNotExistServiceException.class)
    public ResponseEntity<Problem> handleUserForLeasingDoesNotExistException(UserForLeasingDoesNotExistServiceException ex, NativeWebRequest request) {
        return create(new ConflictException(GardleErrorKey.LEASING_USER_DOES_NOT_EXIST, ex.getMessage()), request);
    }

    // Payment providers

    @ExceptionHandler(PaymentProviderServiceException.class)
    public ResponseEntity<Problem> paymentProviderServiceException(PaymentProviderServiceException ex, NativeWebRequest request) {
        return create(new InternalServerErrorException(GardleErrorKey.PAYMENT_PROVIDER_ERROR, ex.getMessage()), request);
    }
}
