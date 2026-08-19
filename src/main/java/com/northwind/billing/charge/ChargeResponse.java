package com.northwind.billing.charge;

import java.math.BigDecimal;

/**
 * Result of charging an invoice. This is a published contract with external consumers —
 * {@code order-service} and the finance reconciliation export both parse it.
 *
 * <p><strong>{@code cardType} carries the card network</strong> — {@code "VISA"} or
 * {@code "MASTERCARD"}, from {@link com.northwind.billing.card.CardNetwork#code()}.
 * Reconciliation groups the daily settlement file by this value and order-service branches on
 * it. Changing the set of values it can return, or changing what the field *means* while
 * keeping its name and type, breaks both consumers silently — there is no schema to fail
 * against, the data simply arrives wrong. Add a new field instead.
 *
 * @param chargeId          our identifier for the charge
 * @param invoiceId         invoice that was charged
 * @param subtotal          invoice amount before tax
 * @param tax               tax from tax-service
 * @param total             amount actually charged
 * @param currency          ISO currency code
 * @param cardType          card network — see above
 * @param acquirerReference the acquirer's reference, needed for settlement and refunds
 * @param status            CHARGED or DECLINED
 */
public record ChargeResponse(
        String chargeId,
        String invoiceId,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        String currency,
        String cardType,
        String acquirerReference,
        String status
) {
}
