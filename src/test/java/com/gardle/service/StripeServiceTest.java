package com.gardle.service;

import com.gardle.GardleApp;
import com.gardle.domain.GardenField;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.service.dto.CreatePaymentProviderAccountDTO;
import com.gardle.service.dto.FinalizePaymentAtPaymentProviderDTO;
import com.gardle.service.dto.GetStripeAccountLinkUrlDTO;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import com.gardle.service.exception.StripeServiceException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = GardleApp.class)
@Transactional
public class StripeServiceTest {

    @MockBean
    private StripeClient stripeClient;

    @MockBean
    private GardenFieldRepository gardenFieldRepository;

    @MockBean
    private SecurityHelperService securityHelperService;

    @Autowired
    private StripeService stripeService;

    private CreatorLeasingDTO creatorLeasingDTO;

    private GardenField gardenField;

    @BeforeEach
    public void init() {
        creatorLeasingDTO = new CreatorLeasingDTO();
        gardenField = new GardenField();
        gardenField.setPricePerM2(1.0);
        gardenField.setSizeInM2(1.0);
        gardenField.setId(1L);

        when(gardenFieldRepository.getOne(1L)).thenReturn(gardenField);
        when(gardenFieldRepository.findById(1L)).thenReturn(Optional.of(gardenField));

        creatorLeasingDTO.setGardenFieldId(gardenField.getId());
        creatorLeasingDTO.setGardenFieldName("gardenfield name");
    }

    @Test
    public void testCreateCheckoutSessionPaymentNull() {
        assertThrows(IllegalArgumentException.class, () ->
            stripeService.createCheckoutSession(null));
    }

    @Test
    public void testCreateCheckoutSession() throws StripeException {
        creatorLeasingDTO.setFrom(Instant.now().plus(100, ChronoUnit.DAYS));
        creatorLeasingDTO.setTo(creatorLeasingDTO.getFrom().plus(100, ChronoUnit.DAYS));
        creatorLeasingDTO.setGardenFieldName("gardenfield name");

        User user = new User();
        user.setId(2L);
        when(securityHelperService.getLoggedInUser()).thenReturn(user);
        when(stripeClient.createCheckoutSession(anyMap())).thenReturn(new Session());

        stripeService.createCheckoutSession(creatorLeasingDTO);
        verify(stripeClient).createCheckoutSession(any());
    }

    @Test
    public void testCancelPayment() throws StripeException {
        String sessionId = "testCheckouId";

        stripeService.cancelPayment(sessionId);

        verify(stripeClient).cancelPaymentIntent(sessionId);
    }

    @Test
    public void testCancelPaymentWithException() throws StripeException {
        String sessionId = "testCheckouId";
        doThrow(InvalidRequestException.class).when(stripeClient).cancelPaymentIntent(anyString());

        assertThrows(StripeServiceException.class, () -> stripeService.cancelPayment(sessionId));
    }

    @Test
    public void testFinalizePayment() throws StripeException {
        String testAccId = "testAccountId";
        String sessionId = "checkoutSessionId";
        Leasing leasing = new Leasing();
        leasing.setFrom(Instant.now().plus(100, ChronoUnit.DAYS));
        leasing.setTo(leasing.getFrom().plus(100, ChronoUnit.DAYS));
        leasing.setPaymentSessionId(sessionId);
        leasing.setGardenField(gardenField);

        stripeService.finalizePayment(new FinalizePaymentAtPaymentProviderDTO(testAccId, leasing));
        verify(stripeClient).capturePaymentIntent(sessionId);
    }

    @Test
    public void testCreateAccount() throws StripeException {
        String bankAccountIBAN = "4000000400000008";
        when(stripeClient.createAccount(anyMap())).thenReturn(new Account());

        stripeService.createAccount(new CreatePaymentProviderAccountDTO("email@mail.com", bankAccountIBAN));

        verify(stripeClient).createAccount(any());
    }

    @Test
    public void testGetAccountLinkUrl() throws StripeException {
        when(stripeClient.getAccountLink(anyMap())).thenReturn("testUrl");

        String accountId = "testAccId";
        String res;

        res = stripeService.getAccountLinkUrl(new GetStripeAccountLinkUrlDTO(accountId, ""));
        assertThat(res).isNotNull();
        assertThat(res).isNotEmpty();
    }

}
