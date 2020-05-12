package com.gardle.service;

import com.gardle.domain.GardenField;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.LeasingState;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.LeasingRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import com.gardle.service.dto.leasing.LeasingDTO;
import com.gardle.service.dto.leasing.LeasingDateRangeDTO;
import com.gardle.service.dto.leasing.UpdatingLeasingDTO;
import com.gardle.service.exception.*;
import com.gardle.service.mapper.CreatorLeasingMapper;
import com.gardle.service.mapper.LeasingMapper;
import com.gardle.service.mapper.UpdatingLeasingMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Leasing}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class LeasingService {

    private final Logger log = LoggerFactory.getLogger(LeasingService.class);

    private final LeasingRepository leasingRepository;
    private final GardenFieldRepository gardenFieldRepository;
    private final UserRepository userRepository;
    private final LeasingMapper leasingMapper;
    private final CreatorLeasingMapper creatorLeasingMapper;
    private final UpdatingLeasingMapper updatingLeasingMapper;
    private final SecurityHelperService securityHelperService;
    private final PaymentService paymentService;
    private final MessageService messageService;

    public final Integer UPDATE_DAY_RANGE = 14; //numbers of days where an status update of the leasing is allowed
    public final Integer CREATE_DAY_RANGE = 14; //number of days before start a leasing can be created
    public final Integer MINIMUM_DAY_RANGE = 7;

    public LeasingDTO createLeasing(CreatorLeasingDTO leasingDTO, String paymentSessionId) {
        log.debug("Request to create leasing request: {}", leasingDTO);
        Leasing leasing = creatorLeasingMapper.toEntity(leasingDTO);

        checkIsCreateAllowedInPeriod(leasing);
        checkMinimumPeriod(leasing);

        User requester = userRepository.findById(leasingDTO.getUserId()).orElseThrow(UserForLeasingDoesNotExistServiceException::new);

        // on creation just open leasings are allowed
        leasing.setStatus(LeasingStatus.OPEN);
        leasing.setUser(requester);
        leasing.setPaymentSessionId(paymentSessionId);

        List<Leasing> overlappings;
        overlappings = leasingRepository.findAllOverlapping(leasing.getGardenField(), requester, leasing.getFrom(), leasing.getTo());

        if (overlappings.size() > 0) {
            throw new LeasingsOverlapServiceException();
        }
        leasing.setStatus(LeasingStatus.OPEN);
        leasing = leasingRepository.save(leasing);
        messageService.createLeasingNotification(leasing);
        return leasingMapper.toDto(leasing);
    }

    public LeasingDTO updateLeasing(UpdatingLeasingDTO leasingDTO) {
        Leasing updatedLeasing = updatingLeasingMapper.toEntity(leasingDTO);
        Leasing currentLeasing = leasingRepository.findById(updatedLeasing.getId()).orElseThrow(LeasingNotFoundServiceException::new);
        if (securityHelperService.loggedInUserIsOwnerOfGardenField(currentLeasing.getGardenField().getId())) {
            updateLeasingForOwnerOfGardenField(currentLeasing, updatedLeasing);
        } else {
            updateLeasingForRequester(currentLeasing, updatedLeasing);
        }
        currentLeasing = leasingRepository.save(currentLeasing);
        messageService.createLeasingNotification(currentLeasing);
        return leasingMapper.toDto(currentLeasing);
    }

    private void updateLeasingForRequester(Leasing currentLeasing, Leasing updatedLeasing) {
        checkIsUpdateAllowedInPeriod(currentLeasing);
        securityHelperService.checkPermission(currentLeasing.getUser().getId());
        // just status change is allowed from open to closed
        if (currentLeasing.getStatus().equals(LeasingStatus.OPEN) &&
            (updatedLeasing.getStatus().equals(LeasingStatus.CANCELLED))) {
            currentLeasing.setStatus(LeasingStatus.CANCELLED);
            paymentService.cancelPayment(currentLeasing.getPaymentSessionId());
        } else if (!currentLeasing.getStatus().equals(updatedLeasing.getStatus())) {
            throw new InvalidLeasingStateServiceException();
        }
    }

    private void updateLeasingForOwnerOfGardenField(Leasing currentLeasing, Leasing updatedLeasing) {
        // just status change is allowed from open to reserved or rejected
        if (currentLeasing.getStatus().equals(LeasingStatus.OPEN)) {
            if (updatedLeasing.getStatus().equals(LeasingStatus.RESERVED)) {
                checkIsUpdateAllowedInPeriod(currentLeasing);
                currentLeasing.setStatus(LeasingStatus.RESERVED);
                paymentService.finalizePayment(currentLeasing);
            } else if (updatedLeasing.getStatus().equals(LeasingStatus.REJECTED)) {
                currentLeasing.setStatus(LeasingStatus.REJECTED);
                paymentService.cancelPayment(currentLeasing.getPaymentSessionId());
            } else {
                throw new InvalidLeasingStateServiceException();
            }
        } else if (!currentLeasing.getStatus().equals(updatedLeasing.getStatus())) {
            throw new InvalidLeasingStateServiceException();
        }
    }

    private void checkIsUpdateAllowedInPeriod(Leasing leasing) {
        if (leasing.getFrom().isBefore(getStartOfToday().plus(UPDATE_DAY_RANGE, ChronoUnit.DAYS))) {
            throw new LeasingUpdateNotAllowedServiceException();
        }
    }

    private void checkIsCreateAllowedInPeriod(Leasing leasing) {
        if (getFromAtStartOfDay(leasing).isBefore(getStartOfToday().plus(CREATE_DAY_RANGE, ChronoUnit.DAYS))) {
            throw new LeasingCreateNotAllowedServiceException();
        }
    }

    private void checkMinimumPeriod(Leasing leasing) {
        if (getFromAtStartOfDay(leasing).plus(MINIMUM_DAY_RANGE, ChronoUnit.DAYS)
            .isAfter(getToAtEndOfDay(leasing))) {
            throw new LeasingTooShortServiceException();
        }
    }

    private Instant getFromAtStartOfDay(Leasing leasing) {
        LocalDate localDate = leasing.getFrom().atZone(ZoneOffset.UTC).toLocalDate();
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private Instant getToAtEndOfDay(Leasing leasing) {
        LocalDate localDate = leasing.getTo().atZone(ZoneOffset.UTC).toLocalDate();
        return localDate.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
    }

    private Instant getStartOfToday() {
        return LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    @Transactional(readOnly = true)
    public Optional<LeasingDTO> findOne(Long id) {
        log.debug("Request to get Leasing : {}", id);
        Leasing leasing = leasingRepository.findById(id).orElseThrow(LeasingNotFoundServiceException::new);
        if (securityHelperService.loggedInUserIsOwnerOfGardenField(leasing.getGardenField().getId())) {
            return Optional.of(leasingMapper.toDto(leasing));
        } else {
            securityHelperService.checkPermission(leasing.getUser().getId());
            return Optional.of(leasingMapper.toDto(leasing));
        }
    }

    public List<LeasingDateRangeDTO> getLeasedDateRanges(Long gardenFieldId, Instant from, Instant to) {
        GardenField gardenField = gardenFieldRepository.findById(gardenFieldId).orElseThrow(GardenFieldNotFoundServiceException::new);
        return leasingRepository.findLeasedDateRangesByFromAndTo(gardenField, LeasingStatus.RESERVED, from, to);
    }

    public Page<LeasingDTO> findByGardenFieldId(Pageable pageable, Long gardenFieldId, List<LeasingStatus> leasingStatusList, Instant from, Instant to, LeasingState state) {
        securityHelperService.checkPermissionByGardenFieldId(gardenFieldId);
        GardenField gardenField = gardenFieldRepository.findById(gardenFieldId)
            .orElseThrow(() -> new GardenFieldUnknownServiceException("Unknown Leasing"));
        if (state == null) {
            return leasingRepository.findAllByGardenFieldAndStatusAndFromAndTo(pageable, gardenField, leasingStatusList, from, to,
                null, null, null).map(leasingMapper::toDto);
        }
        switch (state) {
            case PAST:
                return leasingRepository.findAllByGardenFieldAndStatusAndFromAndTo(pageable, gardenField, leasingStatusList,
                    from, to, Instant.now(), null, null).map(leasingMapper::toDto);
            case ONGOING:
                return leasingRepository.findAllByGardenFieldAndStatusAndFromAndTo(pageable, gardenField, leasingStatusList,
                    from, to, null, Instant.now(), null).map(leasingMapper::toDto);
            case FUTURE:
                return leasingRepository.findAllByGardenFieldAndStatusAndFromAndTo(pageable, gardenField, leasingStatusList,
                    from, to, null, null, Instant.now()).map(leasingMapper::toDto);
            default:
                return leasingRepository.findAllByGardenFieldAndStatusAndFromAndTo(pageable, gardenField, leasingStatusList,
                    from, to, null, null, null).map(leasingMapper::toDto);
        }
    }

    public Page<LeasingDTO> findByUserId(Pageable pageable, Long userId, List<LeasingStatus> leasingStatusList, Instant from, Instant to, LeasingState state) {
        securityHelperService.checkPermission(userId);
        User user = securityHelperService.getLoggedInUser();
        if (state == null) {
            return leasingRepository.findAllByUserAndStatusAndFromAndTo(pageable, user, leasingStatusList, from, to,
                null, null, null).map(leasingMapper::toDto);
        }
        switch (state) {
            case PAST:
                return leasingRepository.findAllByUserAndStatusAndFromAndTo(pageable, user, leasingStatusList,
                    from, to, Instant.now(), null, null).map(leasingMapper::toDto);
            case ONGOING:
                return leasingRepository.findAllByUserAndStatusAndFromAndTo(pageable, user, leasingStatusList,
                    from, to, null, Instant.now(), null).map(leasingMapper::toDto);
            case FUTURE:
                return leasingRepository.findAllByUserAndStatusAndFromAndTo(pageable, user, leasingStatusList,
                    from, to, null, null, Instant.now()).map(leasingMapper::toDto);
            default:
                return leasingRepository.findAllByUserAndStatusAndFromAndTo(pageable, user, leasingStatusList,
                    from, to, null, null, null).map(leasingMapper::toDto);
        }
    }

    public Page<LeasingDTO> findByOwner(Pageable pageable, List<LeasingStatus> leasingStatusList, Instant from, Instant to, LeasingState state) {
        User user = securityHelperService.getLoggedInUser();
        if (state == null) {
            return leasingRepository.findAllByOwnerAndStatusAndFromAndTo(pageable, user, leasingStatusList,
                from, to, null, null, null).map(leasingMapper::toDto);
        }
        switch (state) {
            case PAST:
                return leasingRepository.findAllByOwnerAndStatusAndFromAndTo(pageable, user, leasingStatusList,
                    from, to, Instant.now(), null, null).map(leasingMapper::toDto);
            case ONGOING:
                return leasingRepository.findAllByOwnerAndStatusAndFromAndTo(pageable, user, leasingStatusList,
                    from, to, null, Instant.now(), null).map(leasingMapper::toDto);
            case FUTURE:
                return leasingRepository.findAllByOwnerAndStatusAndFromAndTo(pageable, user, leasingStatusList,
                    from, to, null, null, Instant.now()).map(leasingMapper::toDto);
            default:
                return leasingRepository.findAllByOwnerAndStatusAndFromAndTo(pageable, user, leasingStatusList,
                    from, to, null, null, null).map(leasingMapper::toDto);
        }
    }

    public List<Leasing> getOverLappingLeasingsForGardenfieldAndUserInInterval(final Long gardenfieldId,
                                                                               final Instant from,
                                                                               final Instant to) {
        if (securityHelperService.getLoggedInUser() == null) {
            throw new MissingAuthorityServiceException();
        }
        return this.leasingRepository.findAllOverlapping(
            gardenFieldRepository.findById(gardenfieldId).orElseThrow(GardenFieldNotFoundServiceException::new),
            securityHelperService.getLoggedInUser(),
            from, to);
    }
}
