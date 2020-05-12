package com.gardle.service;

import com.gardle.service.dto.*;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Interface that provides functionality for external payment providers
 */
public interface PaymentProviderService {
    /**
     * cancels the payment associated with the paymentId
     *
     * @param paymentSessionId id of the payment
     */
    void cancelPayment(@NotNull final String paymentSessionId);

    /**
     * Finalizes the payment associated with the leasing and moves the corresponding funds to the account associated
     * with the accountId at the payment provider.
     *
     * @param finalizePaymentAtPaymentProviderDTO dto containing the information to finalize a payment at the external payment provider
     */
    void finalizePayment(@NotNull final FinalizePaymentAtPaymentProviderDTO finalizePaymentAtPaymentProviderDTO);

    /**
     * creates a checkout session for a one time payment
     *
     * @param creatorLeasingDTO contains the checkout session information for the one time payment
     * @return the response dto of the payment checkout
     */
    AbstractCheckoutDTO createCheckoutSession(@NotNull final CreatorLeasingDTO creatorLeasingDTO);

    /**
     * Creates an account at the external payment provider for the corresponding email and the bank account number
     *
     * @param createPaymentProviderAccountDTO the dto containing the data needed to create an account at the external payment provider
     * @return id of the created account at the external payment provider
     */
    String createAccount(@NotNull final CreatePaymentProviderAccountDTO createPaymentProviderAccountDTO);

    /**
     * Returns a unique id for the account associated with the account id
     *
     * @param getStripeAccountLinkUrlDTO holding the accountId which references the account to get the URL for and the generated verification key
     * @return URL of the account associated with the accountId
     */
    String getAccountLinkUrl(@NotNull final GetStripeAccountLinkUrlDTO getStripeAccountLinkUrlDTO);

    /**
     * Checks the signature of a call that was made to one of our webhooks
     *
     * @param checkStripeWebhookSignatureDTO the dto containing the information needed to verify a request received by our webhooks with stripe
     * @return an optional wrapping the signature verified and parsed event object received by our webhook, never null
     */
    @NotNull
    Optional<Event> checkWebhookSignature(@NotNull final CheckStripeWebhookSignatureDTO checkStripeWebhookSignatureDTO);

    /**
     * Returns the paymentIntent object for the given id
     *
     * @param paymentIntentId the id to get the paymentintent object for
     * @return an optional wrapping the paymentintent object, never null
     */
    @NotNull
    Optional<PaymentIntent> getPaymentIntentForId(@Nullable final String paymentIntentId);
}
