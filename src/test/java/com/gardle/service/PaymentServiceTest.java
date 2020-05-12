package com.gardle.service;

import com.gardle.GardleApp;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.service.dto.CreatePaymentProviderAccountDTO;
import com.gardle.service.dto.FinalizePaymentAtPaymentProviderDTO;
import com.gardle.service.exception.BankAccountCreationEmailEmptyServiceException;
import com.gardle.service.exception.PaymentNotSetServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = GardleApp.class)
@Transactional
public class PaymentServiceTest {
    @MockBean
    private SecurityHelperService securityHelperService;
    @MockBean
    private PaymentProviderService paymentProviderService;
    @Autowired
    private PaymentService paymentService;

    @Test
    public void testCancelPayment() {
        doNothing().when(paymentProviderService).cancelPayment(anyString());
        paymentService.cancelPayment("test");

        verify(paymentProviderService).cancelPayment(anyString());
    }

    @Test
    public void testCancelPaymentException() {
        assertThrows(PaymentNotSetServiceException.class, () -> paymentService.cancelPayment(null));
    }

    @Test
    public void testFinalizePayment() {
        Leasing leasing = new Leasing();
        leasing.setPaymentSessionId("testid");
        FinalizePaymentAtPaymentProviderDTO finalizePaymentAtPaymentProviderDTO = new FinalizePaymentAtPaymentProviderDTO(null, leasing);
        doNothing().when(paymentProviderService).finalizePayment(eq(finalizePaymentAtPaymentProviderDTO));
        when(securityHelperService.getLoggedInUser()).thenReturn(new User());

        paymentService.finalizePayment(leasing);

        verify(paymentProviderService).finalizePayment(eq(finalizePaymentAtPaymentProviderDTO));
    }

    @Test
    public void testFinalizePaymentLeasingNullShouldThrowException() {
        assertThrows(PaymentNotSetServiceException.class, () -> paymentService.finalizePayment(null));
    }

    @Test
    public void testFinalizePaymentPaymentSessionIdNullShouldThrowException() {
        Leasing leasing = new Leasing();
        leasing.setPaymentSessionId(null);

        assertThrows(PaymentNotSetServiceException.class, () -> paymentService.finalizePayment(leasing));
    }

    @Test
    public void testCreatePaymentAccount() {
        String email = "test@mail.com";
        String iban = "AT1231231231123";
        CreatePaymentProviderAccountDTO createPaymentProviderAccountDTO = new CreatePaymentProviderAccountDTO(email, iban);
        when(paymentProviderService.createAccount(createPaymentProviderAccountDTO)).thenReturn(null);

        paymentService.createPaymentAccount(email, iban);

        verify(paymentProviderService).createAccount(createPaymentProviderAccountDTO);
    }

    @Test
    public void testCreateAccountEmptyIban() {
        assertThrows(BankAccountCreationEmailEmptyServiceException.class, () -> this.paymentService.createPaymentAccount("test@mail.com", null));
    }


    @Test
    public void testCreateAccountEmptyEmail() {
        assertThrows(BankAccountCreationEmailEmptyServiceException.class, () -> this.paymentService.createPaymentAccount(null, "AT12312312313"));
    }
}
