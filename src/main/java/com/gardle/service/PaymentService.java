package com.gardle.service;

import com.gardle.domain.Leasing;
import com.gardle.service.dto.AbstractCheckoutDTO;
import com.gardle.service.dto.CreatePaymentProviderAccountDTO;
import com.gardle.service.dto.FinalizePaymentAtPaymentProviderDTO;
import com.gardle.service.dto.GetStripeAccountLinkUrlDTO;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import com.gardle.service.exception.BankAccountCreationEmailEmptyServiceException;
import com.gardle.service.exception.PaymentNotSetServiceException;
import com.gardle.service.exception.PaymentProviderServiceException;
import com.gardle.service.exception.PaymentServiceException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final SecurityHelperService securityHelperService;
    private final PaymentProviderService paymentProviderService;

    public AbstractCheckoutDTO createCheckoutSession(final CreatorLeasingDTO creatorLeasingDTO) {
        try {
            return this.paymentProviderService.createCheckoutSession(creatorLeasingDTO);
        } catch (PaymentProviderServiceException e) {
            throw new PaymentServiceException(e.getMessage(), e.getCause());
        }
    }

    public void cancelPayment(String paymentSessionId) {
        if (paymentSessionId == null) {
            throw new PaymentNotSetServiceException("400 payment of leasing to cancel not set", null);
        }
        this.paymentProviderService.cancelPayment(paymentSessionId);
    }

    public void finalizePayment(Leasing leasing) {
        if (leasing == null || leasing.getPaymentSessionId() == null) {
            throw new PaymentNotSetServiceException("400 payment of leasing to finalize not set", null);
        }
        this.paymentProviderService.finalizePayment(new FinalizePaymentAtPaymentProviderDTO(securityHelperService.getLoggedInUser().getStripeAccountId(), leasing));
    }

    public String createPaymentAccount(@Nullable String email, @Nullable final String bankAccountIBAN) {
        if (StringUtils.isEmpty(bankAccountIBAN)) {
            throw new BankAccountCreationEmailEmptyServiceException("bank account number must not be empty", null);
        }
        if (StringUtils.isEmpty(email)) {
            throw new BankAccountCreationEmailEmptyServiceException();
        }
        return this.paymentProviderService.createAccount(new CreatePaymentProviderAccountDTO(email, bankAccountIBAN));
    }

    public String getAccountLinkUrl(@NotNull final GetStripeAccountLinkUrlDTO getStripeAccountLinkUrlDTO) {
        return this.paymentProviderService.getAccountLinkUrl(getStripeAccountLinkUrlDTO);
    }
}
