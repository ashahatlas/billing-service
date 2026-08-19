package com.northwind.billing.charge;

import jakarta.validation.constraints.NotBlank;

/**
 * A request to charge an invoice. The amount is never supplied by the caller — it comes from
 * the invoice, and tax is resolved server-side.
 */
public record ChargeRequest(

        @NotBlank(message = "cardNumber is required")
        String cardNumber,

        @NotBlank(message = "currency is required")
        String currency,

        String billingPostcode
) {
}
