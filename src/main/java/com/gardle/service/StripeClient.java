package com.gardle.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(StripeClient.class);

    @Autowired
    private Environment environment;

    @Value("${stripe.private-key}")
    private String stripeApiKey;

    @PostConstruct
    private void init() {
        Stripe.apiKey = this.stripeApiKey;
        LOGGER.debug("after init, stripe api key is {} for current environment: {}", Stripe.apiKey, environment.getActiveProfiles());
    }

    public Session createCheckoutSession(final Map<String, Object> params) throws StripeException {
        return Session.create(params);
    }

    public void cancelPaymentIntent(final String stripeCheckoutSessionId) throws StripeException {
        PaymentIntent paymentIntent = this.getPaymentIntentForCheckoutSessionId(stripeCheckoutSessionId);
        paymentIntent.cancel();
        LOGGER.debug("after payment intent cancel, paymentIntent: {}", paymentIntent);
    }

    public void capturePaymentIntent(final String stripeCheckoutSessionId) throws StripeException {
        PaymentIntent paymentIntent = this.getPaymentIntentForCheckoutSessionId(stripeCheckoutSessionId);
        paymentIntent.capture();
        LOGGER.debug("after paymentIntent capture, paymentIntent: {}", paymentIntent);
    }

    private PaymentIntent getPaymentIntentForCheckoutSessionId(final String checkoutSessionId) throws StripeException {
        Session session = Session.retrieve(checkoutSessionId);
        LOGGER.debug("after retrieving checkoutSession, session: {}", session);
        return PaymentIntent.retrieve(session.getPaymentIntent());
    }

    public Account createAccount(@NotNull final Map<String, Object> params) throws StripeException {
        LOGGER.debug("creating account with params: {}", params);
        return Account.create(params);
    }

    public String getAccountLink(@NotNull final Map<String, Object> params) throws StripeException {
        LOGGER.debug("getting account link with params {}", params);
        return AccountLink.create(params).getUrl();
    }

    public Payout doPayout(final Map<String, Object> params, final RequestOptions requestOptions) throws StripeException {
        LOGGER.debug("doing payout with params: {} and request options: {}", params, requestOptions);
        return Payout.create(params, requestOptions);
    }

    public Event checkWebhookSignature(String payload, String stripeSignatureHeader, String endpointSecret) throws SignatureVerificationException {
        LOGGER.debug("checking webhook signature {} for payload {} with endpoint secret {}", stripeSignatureHeader, payload, endpointSecret);
        return Webhook.constructEvent(payload, stripeSignatureHeader, endpointSecret);
    }

    public PaymentIntent retreivePaymentIntentForId(final String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }
}
