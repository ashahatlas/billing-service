package com.northwind.billing.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Client for {@code tax-service}, which owns tax rules and rates.
 *
 * <p>Hard dependency — we do not charge an amount we have not taxed correctly, so there is no
 * fallback. Timeouts are set to tax-service's published p99 of 400ms.
 */
@Component
public class TaxServiceClient {

    static final Duration CONNECT_TIMEOUT = Duration.ofMillis(200);
    static final Duration READ_TIMEOUT = Duration.ofMillis(500);

    private final RestClient restClient;

    public TaxServiceClient(@Value("${clients.tax.baseUrl}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public BigDecimal taxFor(String invoiceId, BigDecimal subtotal, String currency, String postcode) {
        return restClient.get()
                .uri("/v1/tax?invoiceId={invoiceId}&amount={amount}&currency={currency}&postcode={postcode}",
                        invoiceId, subtotal, currency, postcode)
                .retrieve()
                .body(TaxResult.class)
                .taxAmount();
    }

    record TaxResult(BigDecimal taxAmount, String rateId) {
    }
}
