package com.gardle.service;

import com.gardle.GardleApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = GardleApp.class)
@Transactional
public class StripeWebhookServiceTest {
    @MockBean
    private StripeService stripeService;

    @Autowired
    private StripeWebhookService stripeWebhookService;

    @Test
    public void testHandleLeasingWebhook() {
        when(stripeService.checkWebhookSignature(any())).thenReturn(Optional.empty());

        stripeWebhookService.handleLeasingWebhook("test", "signature handler");
        verify(stripeService).checkWebhookSignature(any());
    }
}
