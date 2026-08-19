# Charge API

## `POST /v1/invoices/{invoiceId}/charge`

Charges an invoice against a card. Applies tax from `tax-service`, settles through the
Worldpay acquirer.

```json
{
  "cardNumber": "4111111111111111",
  "currency": "GBP",
  "billingPostcode": "EC2A 4BX"
}
```

### Response

```json
{
  "chargeId": "chg_9f3b7c21",
  "invoiceId": "inv-1001",
  "subtotal": "249.00",
  "surcharge": "0.00",
  "tax": "49.80",
  "total": "298.80",
  "currency": "GBP",
  "cardType": "CREDIT",
  "cardNetwork": "VISA",
  "acquirerReference": "wp_4f8a21c7",
  "status": "CHARGED"
}
```

## `POST /v1/charges`

The new consolidated endpoint. Same response, but the invoice goes in the body so callers no
longer need a separate lookup before charging.

```json
{
  "invoiceId": "inv-1001",
  "cardNumber": "378282246310005",
  "currency": "GBP",
  "billingPostcode": "EC2A 4BX"
}
```

Amex charges carry a 1.5% surcharge, applied to the subtotal before tax, and settle through
Amex Direct rather than Worldpay — their `acquirerReference` is prefixed `amex_`.

## Contract stability

This response is consumed outside this service. Treat it as versioned even though there is no
version in the path.

| Field | Consumer | Used for |
| --- | --- | --- |
| `cardNetwork` | `order-service` | order state and receipt template |
| `cardNetwork` | finance reconciliation | groups the daily settlement file by network |
| `acquirerReference` | finance reconciliation, refunds | matching our charges to the acquirer's settlement report |
| `total`, `status` | `order-service` | order total and whether to release the order |

### `cardType` values

`CREDIT` or `CHARGE_CARD`. Amex is a charge card; Visa and Mastercard are credit.

Previously this field carried the card network, which was always a slightly loose use of the
name. With Amex in the mix the funding distinction genuinely matters, so the network moved to
`cardNetwork` and `cardType` now says what it sounds like it says. Nothing is lost — the
network is still on the response, one field along.

## Errors

| Status | When |
| --- | --- |
| `400` | card number missing or the network is not one we accept |
| `404` | invoice not found |
| `502` | `tax-service` could not price the tax — we do not charge an untaxed amount |
