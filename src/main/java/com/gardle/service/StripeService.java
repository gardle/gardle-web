package com.gardle.service;

import com.gardle.service.dto.*;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import com.gardle.service.exception.StripeServiceException;
import com.gardle.service.mapper.CreatorLeasingMapper;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StripeService implements PaymentProviderService {
    private final Logger log = LoggerFactory.getLogger(StripeService.class);
    private final CreatorLeasingMapper creatorLeasingMapper;
    private final StripeClient stripeClient;
    private final SecurityHelperService securityHelperService;

    @Value("${stripe.public-key}")
    private String publicKey;
    @Value("${stripe.private-key}")
    private String privateKey;
    @Value("${stripe.payment-success-url}")
    private String paymentSuccessUrl;
    @Value("${stripe.payment-cancel-url}")
    private String paymentCancelUrl;
    @Value("${stripe.account-success-url}")
    private String accountSuccessUrl;
    @Value("${stripe.account-failure-url}")
    private String accountFailureUrl;

    @PostConstruct
    private void init() {
        Stripe.apiKey = this.privateKey;
    }

    @Override
    public AbstractCheckoutDTO createCheckoutSession(final CreatorLeasingDTO creatorLeasingDTO) {
        if (creatorLeasingDTO == null) {
            throw new IllegalArgumentException("payment dto null");
        }
        try {
            Session session = this.stripeClient.createCheckoutSession(
                createCheckoutSessionRequestParamsFromCreatorLeasingDTO(creatorLeasingDTO));
            AbstractCheckoutDTO abstractCheckoutDTO = new AbstractCheckoutDTO();
            abstractCheckoutDTO.setId(session.getId());
            return abstractCheckoutDTO;
        } catch (StripeException e) {
            e.printStackTrace();
            throw new StripeServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void cancelPayment(final String checkoutSessionId) {
        try {
            this.stripeClient.cancelPaymentIntent(checkoutSessionId);
        } catch (StripeException e) {
            throw new StripeServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void finalizePayment(@NotNull final FinalizePaymentAtPaymentProviderDTO finalizePaymentAtPaymentProviderDTO) {
        try {
            this.stripeClient.capturePaymentIntent(finalizePaymentAtPaymentProviderDTO.getLeasing().getPaymentSessionId());
//            this.payout(finalizePaymentAtPaymentProviderDTO.getAccountId(), finalizePaymentAtPaymentProviderDTO.getLeasing().getPriceSumInCents());
        } catch (StripeException e) {
            throw new StripeServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public String createAccount(final CreatePaymentProviderAccountDTO createPaymentProviderAccountDTO) {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> externalAccountInfo = new HashMap<>();
        externalAccountInfo.put("object", "bank_account");
        externalAccountInfo.put("country", "AT");
        externalAccountInfo.put("currency", "eur");
        externalAccountInfo.put("account_number", createPaymentProviderAccountDTO.getBankAccountIBAN());

        params.put("country", "AT");
        params.put("type", "custom");
        params.put("requested_capabilities", Arrays.asList("card_payments", "transfers"));
        params.put("external_account", externalAccountInfo);
        params.put("business_type", "individual");
        Map<String, Object> individualInfo = new HashMap<>(); // needed by stripe, since businesstype is individual https://stripe.com/docs/connect/custom-accounts
        individualInfo.put("email", createPaymentProviderAccountDTO.getEmail());
        params.put("individual", individualInfo);

        try {
            log.debug("creating account with info: {}", params);
            return this.stripeClient.createAccount(params).getId();
        } catch (StripeException e) {
            throw new StripeServiceException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public String getAccountLinkUrl(@NotNull final GetStripeAccountLinkUrlDTO getStripeAccountLinkUrlDTO) {
        Map<String, Object> params = new HashMap<>();
        params.put("account", getStripeAccountLinkUrlDTO.getStripeAccountId());
        params.put("failure_url", accountFailureUrl);
        params.put("success_url", accountSuccessUrl + "?stripeVerificationKey=" + getStripeAccountLinkUrlDTO.getStripeVerificationKey());
        params.put("type", "custom_account_verification");
        params.put("collect", "eventually_due");

        try {
            return this.stripeClient.getAccountLink(params);
        } catch (StripeException e) {
            throw new StripeServiceException(e.getMessage(), e.getCause());
        }
    }

    private void payout(final String accountId, final Integer amount) {
        log.debug("Payout for " + accountId + " with amount " + amount + " triggered");
        RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(accountId).build();

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", "eur");
        try {
            this.stripeClient.doPayout(params, requestOptions);
            log.debug("Payout was successful");
        } catch (StripeException e) {
            throw new StripeServiceException(e.getMessage(), e.getCause());
        }
    }

    private Map<String, Object> createCheckoutSessionRequestParamsFromCreatorLeasingDTO(final CreatorLeasingDTO creatorLeasingDTO) {
        Map<String, Object> paymentIntentData = new HashMap<>();
        paymentIntentData.put("capture_method", "manual");

        Map<String, String> paymentIntentMetaData = new HashMap<>();
        paymentIntentMetaData.put("leasing_from", creatorLeasingDTO.getFrom().toString());
        paymentIntentMetaData.put("leasing_to", creatorLeasingDTO.getTo().toString());
        paymentIntentMetaData.put("leasing_gardenfield_id", creatorLeasingDTO.getGardenFieldId().toString());
        paymentIntentMetaData.put("leasing_requester_id", securityHelperService.getLoggedInUser().getId().toString());

        paymentIntentData.put("metadata", paymentIntentMetaData);

        List<Object> paymentMethodTypes = new ArrayList<>();
        paymentMethodTypes.add("card");
        List<Object> lineItems = new ArrayList<>();
        Map<String, Object> lineItem1 = new HashMap<>();
        lineItem1.put("name", creatorLeasingDTO.getGardenFieldName());
        lineItem1.put("amount", creatorLeasingMapper.toEntity(creatorLeasingDTO).getPriceSumInCents());
        lineItem1.put("currency", "eur");
        lineItem1.put("quantity", 1);
        lineItems.add(lineItem1);
        Map<String, Object> params = new HashMap<>();
        params.put("success_url", paymentSuccessUrl);
        params.put("cancel_url", paymentCancelUrl);
        params.put("payment_method_types", paymentMethodTypes);
        params.put("line_items", lineItems);
        params.put("payment_intent_data", paymentIntentData);

        return params;
    }

    @NotNull
    public Optional<Event> checkWebhookSignature(@NotNull final CheckStripeWebhookSignatureDTO checkStripeWebhookSignatureDTO) {
        try {
            return Optional.of(this.stripeClient.checkWebhookSignature(
                checkStripeWebhookSignatureDTO.getPayload(), checkStripeWebhookSignatureDTO.getStripeSignatureHeader(),
                checkStripeWebhookSignatureDTO.getEndpointSecret()));
        } catch (SignatureVerificationException e) {
            throw new StripeServiceException(e.getMessage(), e.getCause());
        }
    }

    @NotNull
    public Optional<PaymentIntent> getPaymentIntentForId(@Nullable final String paymentIntentId) {
        try {
            return Optional.of(this.stripeClient.retreivePaymentIntentForId(paymentIntentId));
        } catch (StripeException e) {
            throw new StripeServiceException(e.getMessage(), e.getCause());
        }
    }
}
