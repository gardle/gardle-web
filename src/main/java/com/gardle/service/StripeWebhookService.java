package com.gardle.service;

import com.gardle.service.dto.CheckStripeWebhookSignatureDTO;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import com.stripe.model.Account;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StripeWebhookService.class);
    private final LeasingService leasingService;
    private final UserService userService;
    private final PaymentProviderService stripeService;

    @Value("${stripe.leasing-webhook-secret}")
    private String leasingEndpointSecret;
    @Value("${stripe.account-webhook-secret}")
    private String accountsEndpointSecret;

    public void handleLeasingWebhook(final String payload, final String stripeSignatureHeader) {
        stripeService.checkWebhookSignature(new CheckStripeWebhookSignatureDTO(payload, stripeSignatureHeader, leasingEndpointSecret)).ifPresent((event -> {
            // Handle the checkout.session.completed event
            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                onCheckoutSessionCompleted(session);
            }
        }));
    }

    private void onCheckoutSessionCompleted(@Nullable final Session session) {
        if (session != null) {
            this.stripeService.getPaymentIntentForId(session.getPaymentIntent())
                .ifPresent((paymentIntent) -> leasingService.createLeasing(extractData(paymentIntent.getMetadata()), session.getId()));
        }
    }

    public void handleAccountUpdateWebhook(final String payload, final String stripeSignatureHeader) {
        stripeService.checkWebhookSignature(new CheckStripeWebhookSignatureDTO(payload, stripeSignatureHeader, accountsEndpointSecret)).ifPresent((event) -> {
            if ("account.updated".equals(event.getType())) {
                Account account = (Account) event.getDataObjectDeserializer().getObject().orElse(null);
                onAccountUpdate(account);
            }
        });
    }

    private void onAccountUpdate(final Account account) {
        LOGGER.debug("Received account update webhook, with account: {}", account);
        if (account != null) {
            if (account.getPayoutsEnabled()) {
                userService.getUserByStripeAccountId(account.getId()).ifPresent(userToUpdate -> {
                    userToUpdate.setStripeAccountVerified(true);
                    userService.updateUser(userToUpdate);
                });
            } else {
                LOGGER.debug("payout not possible, needed requirements: {}", account.getRequirements());
            }
        } else {
            LOGGER.debug("Error in Account Object deserialization in StripeWebhookService, account nr");
        }
    }

    private CreatorLeasingDTO extractData(Map<String, String> metaData) {
        CreatorLeasingDTO creatorLeasingDTO = new CreatorLeasingDTO();
        creatorLeasingDTO.setFrom(Instant.parse(metaData.get("leasing_from")));
        creatorLeasingDTO.setTo(Instant.parse(metaData.get("leasing_to")));
        creatorLeasingDTO.setGardenFieldId(Long.valueOf(metaData.get("leasing_gardenfield_id")));
        creatorLeasingDTO.setUserId(Long.valueOf(metaData.get("leasing_requester_id")));
        return creatorLeasingDTO;
    }
}
