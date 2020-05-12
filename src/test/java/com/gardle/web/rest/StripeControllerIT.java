package com.gardle.web.rest;

import com.gardle.GardleApp;
import com.gardle.service.StripeWebhookService;
import com.gardle.service.exception.StripeServiceException;
import com.gardle.web.rest.errors.ExceptionTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import static com.gardle.web.rest.TestUtil.createFormattingConversionService;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link StripeController} REST controller.
 */
@SpringBootTest(classes = GardleApp.class)
public class StripeControllerIT {
    private MockMvc restStripeMockMvc;
    @MockBean
    private StripeWebhookService stripeWebhookService;
    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    @BeforeEach
    public void init() {
        StripeController stripeController = new StripeController(stripeWebhookService);

        this.restStripeMockMvc = MockMvcBuilders.standaloneSetup(stripeController)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    @Test
    @Transactional
    public void createLeasing() throws Exception {
        String payload = "test";
        String signHeader = "test";
        doNothing().when(stripeWebhookService).handleLeasingWebhook(payload, signHeader);

        restStripeMockMvc.perform(post("/stripe/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .header("Stripe-Signature", signHeader)
            .content(TestUtil.convertObjectToJsonBytes(payload)))
            .andExpect(status().isOk());

        verify(stripeWebhookService).handleLeasingWebhook(payload, signHeader);
    }

    @Test
    @Transactional
    public void createOverlappingLeasingWithSameUserAndStatusOpen() throws Exception {
        String payload = "test";
        String signHeader = "test";
        doThrow(StripeServiceException.class).when(stripeWebhookService).handleLeasingWebhook(payload, signHeader);

        restStripeMockMvc.perform(post("/stripe/leasings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .header("Stripe-Signature", signHeader)
            .content(TestUtil.convertObjectToJsonBytes(payload)))
            .andExpect(status().isInternalServerError());

        verify(stripeWebhookService).handleLeasingWebhook(payload, signHeader);
    }

    @Test
    @Transactional
    public void createOverlappingLeasingWithOtherUserAndReservedStatus() throws Exception {
        String payload = "test";
        String signHeader = "test";
        doNothing().when(stripeWebhookService).handleAccountUpdateWebhook(payload, signHeader);

        restStripeMockMvc.perform(post("/stripe/accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .header("Stripe-Signature", signHeader)
            .content(TestUtil.convertObjectToJsonBytes(payload)))
            .andExpect(status().isOk());

        verify(stripeWebhookService).handleAccountUpdateWebhook(payload, signHeader);
    }

    @Test
    @Transactional
    public void createSecondNonOverlappingLeasing() throws Exception {
        String payload = "test";
        String signHeader = "test";
        doThrow(StripeServiceException.class).when(stripeWebhookService).handleAccountUpdateWebhook(payload, signHeader);

        restStripeMockMvc.perform(post("/stripe/accounts")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .header("Stripe-Signature", signHeader)
            .content(TestUtil.convertObjectToJsonBytes(payload)))
            .andExpect(status().isInternalServerError());

        verify(stripeWebhookService).handleAccountUpdateWebhook(payload, signHeader);
    }

}
