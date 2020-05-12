package com.gardle.service;

import com.gardle.GardleApp;
import com.gardle.domain.GardenField;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.repository.LeasingRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.dto.SimpleGardenFieldDTO;
import com.gardle.service.dto.SimpleUserDTO;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import com.gardle.service.dto.leasing.LeasingDTO;
import com.gardle.service.dto.leasing.UpdatingLeasingDTO;
import com.gardle.service.exception.*;
import com.gardle.service.mapper.CreatorLeasingMapper;
import com.gardle.service.mapper.LeasingMapper;
import com.gardle.service.mapper.UpdatingLeasingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = GardleApp.class)
@Transactional
public class LeasingServiceTest {

    private final User requester = new User();
    private final User owner = new User();
    private final GardenField gardenField = new GardenField();
    private final SimpleUserDTO simpleRequester = new SimpleUserDTO();
    private final SimpleGardenFieldDTO simpleGardenField = new SimpleGardenFieldDTO();

    @MockBean
    private LeasingRepository leasingRepository;
    @MockBean
    private LeasingMapper leasingMapper;
    @MockBean
    private CreatorLeasingMapper creatorLeasingMapper;
    @MockBean
    private UpdatingLeasingMapper updatingLeasingMapper;
    @MockBean
    private SecurityHelperService securityHelperService;
    private static final String DEFAULT_PAYMENT_SESSION_ID = "cs_test_m5CBqcXSIJeKW7Ijb5vp8D9BrvDlJ6lRn25m6BdTZ0a1cLbIz5xVQ7bX";
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private LeasingService leasingService;
    private Leasing leasing;
    private LeasingDTO leasingDTO;
    private CreatorLeasingDTO creatorLeasingDTO;
    private UpdatingLeasingDTO updatingLeasingDTO;
    @MockBean
    private PaymentService paymentService;

    @BeforeEach
    public void init() {
        requester.setId(1L);
        simpleRequester.setId(1L);
        owner.setId(2L);
        gardenField.setId(1L);
        gardenField.setName("testField");
        gardenField.setOwner(owner);

        simpleGardenField.setId(1L);
        simpleGardenField.setName("testField");

        Instant from = Instant.now().plus(100, ChronoUnit.DAYS);
        Instant to = Instant.now().plus(200, ChronoUnit.DAYS);

        leasing = new Leasing();
        leasing.setGardenField(gardenField);
        leasing.setUser(requester);
        leasing.setFrom(from);
        leasing.setTo(to);
        leasing.setStatus(LeasingStatus.OPEN);

        leasingDTO = new LeasingDTO();
        leasingDTO.setGardenField(simpleGardenField);
        leasingDTO.setUser(simpleRequester);
        leasingDTO.setFrom(from);
        leasingDTO.setTo(to);
        leasingDTO.setStatus(LeasingStatus.OPEN);

        creatorLeasingDTO = new CreatorLeasingDTO();
        creatorLeasingDTO.setFrom(from);
        creatorLeasingDTO.setTo(to);
        creatorLeasingDTO.setGardenFieldId(simpleGardenField.getId());
        creatorLeasingDTO.setUserId(requester.getId());

        updatingLeasingDTO = new UpdatingLeasingDTO();

        when(leasingMapper.toEntity(leasingDTO)).thenReturn(leasing);
        when(leasingMapper.toDto(leasing)).thenReturn(leasingDTO);

        when(creatorLeasingMapper.toEntity(creatorLeasingDTO)).thenReturn(leasing);
        when(creatorLeasingMapper.toDto(leasing)).thenReturn(creatorLeasingDTO);

        when(updatingLeasingMapper.toEntity(updatingLeasingDTO)).thenReturn(leasing);

        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
    }

    @Test
    public void testCreateJustLoggedInUser() {
        when(userRepository.findById(requester.getId())).thenReturn(Optional.empty());
        assertThrows(UserForLeasingDoesNotExistServiceException.class,
            () -> leasingService.createLeasing(creatorLeasingDTO, DEFAULT_PAYMENT_SESSION_ID));
    }

    @Test
    public void testCreateJustLeasingsWithoutOverlaps() {
        List<Leasing> overlappings = new ArrayList<>();
        overlappings.add(new Leasing());
        when(leasingRepository.findAllOverlapping(gardenField, requester, leasing.getFrom(), leasing.getTo())).thenReturn(overlappings);
        assertThrows(LeasingsOverlapServiceException.class, () -> leasingService.createLeasing(creatorLeasingDTO, DEFAULT_PAYMENT_SESSION_ID));
    }

    @Test
    public void testCreateJustInSpecificTimePeriodAllowed() {
        leasing.setFrom(Instant.now().plus(13, ChronoUnit.DAYS));
        when(leasingRepository.findById(leasing.getId())).thenReturn(Optional.of(leasing));
        assertThrows(LeasingCreateNotAllowedServiceException.class, () -> leasingService.createLeasing(creatorLeasingDTO, DEFAULT_PAYMENT_SESSION_ID));
    }

    @Test
    public void testCreateJustOverAMinimumPeriodAllowed() {
        leasing.setFrom(Instant.now().plus(20, ChronoUnit.DAYS));
        leasing.setTo(leasing.getFrom().plus(6, ChronoUnit.DAYS));
        assertThrows(LeasingTooShortServiceException.class, () -> leasingService.createLeasing(creatorLeasingDTO, DEFAULT_PAYMENT_SESSION_ID));
    }

    @Test
    public void testCreateLeasingSuccessfullyBoundaries() {
        leasing.setFrom(Instant.now().plus(15, ChronoUnit.DAYS));
        leasing.setTo(leasing.getFrom().plus(8, ChronoUnit.DAYS));

        when(leasingRepository.findAllOverlapping(gardenField, requester, leasing.getFrom(), leasing.getTo())).thenReturn(new ArrayList<>());
        when(leasingRepository.save(leasing)).thenReturn(leasing);
        assertThat(leasingService.createLeasing(creatorLeasingDTO, DEFAULT_PAYMENT_SESSION_ID), is(leasingDTO));
    }

    @Test
    public void testCreateLeasingSuccessfully() {
        when(leasingRepository.findAllOverlapping(gardenField, requester, leasing.getFrom(), leasing.getTo())).thenReturn(new ArrayList<>());
        when(leasingRepository.save(leasing)).thenReturn(leasing);
        assertThat(leasingService.createLeasing(creatorLeasingDTO, DEFAULT_PAYMENT_SESSION_ID), is(leasingDTO));
    }

    @Test
    public void testUpdateJustExistingLeasing() {
        when(leasingRepository.findById(leasing.getId())).thenReturn(Optional.empty());
        assertThrows(LeasingNotFoundServiceException.class, () -> leasingService.updateLeasing(updatingLeasingDTO));
    }

    @Test
    public void testUpdateJustInSpecificTimePeriodAllowed() {
        leasing.setFrom(Instant.now().plus(13, ChronoUnit.DAYS));
        when(leasingRepository.findById(leasing.getId())).thenReturn(Optional.of(leasing));
        assertThrows(LeasingUpdateNotAllowedServiceException.class, () -> leasingService.updateLeasing(updatingLeasingDTO));
    }

    @Test
    public void testUpdateOwnerWantToChangeStatusToOpen() {
        leasing.setStatus(LeasingStatus.RESERVED);
        when(leasingRepository.findById(leasing.getId())).thenReturn(Optional.of(leasing));
        Leasing newLeasing = new Leasing();
        newLeasing.setStatus(LeasingStatus.OPEN);
        when(updatingLeasingMapper.toEntity(updatingLeasingDTO)).thenReturn(newLeasing);
        assertThrows(InvalidLeasingStateServiceException.class, () -> leasingService.updateLeasing(updatingLeasingDTO));
    }

    @Test
    public void testUpdateLeasingByOwnerSuccessfullySetStatusReserved() {
        when(securityHelperService.loggedInUserIsOwnerOfGardenField(gardenField.getId())).thenReturn(true);

        Leasing newLeasing = new Leasing();
        newLeasing.setStatus(LeasingStatus.RESERVED);
        newLeasing.setGardenField(gardenField);
        newLeasing.setUser(requester);
        newLeasing.setFrom(leasing.getFrom());
        newLeasing.setTo(leasing.getTo());

        LeasingDTO newLeasingDTO = new LeasingDTO();
        newLeasingDTO.setStatus(LeasingStatus.RESERVED);
        newLeasingDTO.setGardenField(simpleGardenField);
        newLeasingDTO.setUser(simpleRequester);
        newLeasingDTO.setFrom(leasing.getFrom());
        newLeasingDTO.setTo(leasing.getTo());

        UpdatingLeasingDTO newUpdatingLeasingDTO = new UpdatingLeasingDTO();
        newUpdatingLeasingDTO.setStatus(LeasingStatus.RESERVED);
        newUpdatingLeasingDTO.setGardenFieldId(simpleGardenField.getId());

        when(leasingRepository.findById(leasing.getId())).thenReturn(Optional.of(leasing));
        when(updatingLeasingMapper.toEntity(newUpdatingLeasingDTO)).thenReturn(newLeasing);
        when(leasingMapper.toDto(newLeasing)).thenReturn(newLeasingDTO);
        when(leasingRepository.save(newLeasing)).thenReturn(newLeasing);
        assertThat(leasingService.updateLeasing(newUpdatingLeasingDTO), is(newLeasingDTO));
        verify(paymentService, times(1)).finalizePayment(leasing);
    }

    @Test
    public void testUpdateLeasingByOwnerSuccessfullySetStatusRejected() {
        when(securityHelperService.loggedInUserIsOwnerOfGardenField(gardenField.getId())).thenReturn(true);

        Leasing newLeasing = new Leasing();
        newLeasing.setStatus(LeasingStatus.REJECTED);
        newLeasing.setGardenField(gardenField);
        newLeasing.setUser(requester);
        newLeasing.setFrom(leasing.getFrom());
        newLeasing.setTo(leasing.getTo());

        LeasingDTO newLeasingDTO = new LeasingDTO();
        newLeasingDTO.setStatus(LeasingStatus.REJECTED);
        newLeasingDTO.setGardenField(simpleGardenField);
        newLeasingDTO.setUser(simpleRequester);
        newLeasingDTO.setFrom(leasing.getFrom());
        newLeasingDTO.setTo(leasing.getTo());

        UpdatingLeasingDTO newUpdatingLeasingDTO = new UpdatingLeasingDTO();
        newUpdatingLeasingDTO.setStatus(LeasingStatus.REJECTED);
        newUpdatingLeasingDTO.setGardenFieldId(simpleGardenField.getId());

        when(leasingRepository.findById(leasing.getId())).thenReturn(Optional.of(leasing));
        when(updatingLeasingMapper.toEntity(newUpdatingLeasingDTO)).thenReturn(newLeasing);
        when(leasingMapper.toDto(newLeasing)).thenReturn(newLeasingDTO);
        when(leasingRepository.save(newLeasing)).thenReturn(newLeasing);
        assertThat(leasingService.updateLeasing(newUpdatingLeasingDTO), is(newLeasingDTO));
        verify(paymentService, times(1)).cancelPayment(leasing.getPaymentSessionId());
    }

    @Test
    public void testUpdateLeasingByRequesterNotAuthorizedUser() {
        doThrow(new MissingPermissionServiceException()).when(securityHelperService).checkPermission(requester.getId());
        when(leasingRepository.findById(leasing.getId())).thenReturn(Optional.of(leasing));
        assertThrows(MissingPermissionServiceException.class, () -> leasingService.updateLeasing(updatingLeasingDTO));
    }

    @Test
    public void testUpdateRequesterWantToChangeStatusToReserved() {
        when(securityHelperService.loggedInUserIsOwnerOfGardenField(gardenField.getId())).thenReturn(false);
        when(securityHelperService.getLoggedInUser()).thenReturn(requester);

        leasing.setStatus(LeasingStatus.OPEN);
        when(leasingRepository.findById(leasing.getId())).thenReturn(Optional.of(leasing));
        Leasing newLeasing = new Leasing();
        newLeasing.setStatus(LeasingStatus.RESERVED);
        when(updatingLeasingMapper.toEntity(updatingLeasingDTO)).thenReturn(newLeasing);
        assertThrows(InvalidLeasingStateServiceException.class, () -> leasingService.updateLeasing(updatingLeasingDTO));
    }

    @Test
    public void testUpdateRequesterWantToChangeStatusToOpen() {
        when(securityHelperService.loggedInUserIsOwnerOfGardenField(gardenField.getId())).thenReturn(false);
        when(securityHelperService.getLoggedInUser()).thenReturn(requester);

        leasing.setStatus(LeasingStatus.CANCELLED);
        when(leasingRepository.findById(leasing.getId())).thenReturn(Optional.of(leasing));
        Leasing newLeasing = new Leasing();
        newLeasing.setStatus(LeasingStatus.OPEN);
        when(updatingLeasingMapper.toEntity(updatingLeasingDTO)).thenReturn(newLeasing);
        assertThrows(InvalidLeasingStateServiceException.class, () -> leasingService.updateLeasing(updatingLeasingDTO));
    }

    @Test
    public void testUpdateLeasingByRequesterSuccessfullySetStatusCancelled() {
        when(securityHelperService.loggedInUserIsOwnerOfGardenField(gardenField.getId())).thenReturn(false);
        when(securityHelperService.getLoggedInUser()).thenReturn(requester);

        Leasing newLeasing = new Leasing();
        newLeasing.setStatus(LeasingStatus.CANCELLED);
        newLeasing.setGardenField(gardenField);
        newLeasing.setUser(requester);
        newLeasing.setFrom(leasing.getFrom());
        newLeasing.setTo(leasing.getTo());

        LeasingDTO newLeasingDTO = new LeasingDTO();
        newLeasingDTO.setStatus(LeasingStatus.CANCELLED);
        newLeasingDTO.setGardenField(simpleGardenField);
        newLeasingDTO.setUser(simpleRequester);
        newLeasingDTO.setFrom(leasing.getFrom());
        newLeasingDTO.setTo(leasing.getTo());

        UpdatingLeasingDTO newUpdatingLeasingDTO = new UpdatingLeasingDTO();
        newUpdatingLeasingDTO.setStatus(LeasingStatus.CANCELLED);
        newUpdatingLeasingDTO.setGardenFieldId(simpleGardenField.getId());

        when(leasingRepository.findById(leasing.getId())).thenReturn(Optional.of(leasing));
        when(updatingLeasingMapper.toEntity(newUpdatingLeasingDTO)).thenReturn(newLeasing);
        when(leasingMapper.toDto(newLeasing)).thenReturn(newLeasingDTO);
        when(leasingRepository.save(newLeasing)).thenReturn(newLeasing);
        assertThat(leasingService.updateLeasing(newUpdatingLeasingDTO), is(newLeasingDTO));
        verify(paymentService, times(1)).cancelPayment(leasing.getPaymentSessionId());
    }

}
