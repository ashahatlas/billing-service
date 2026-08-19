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
  "tax": "49.80",
  "total": "298.80",
  "currency": "GBP",
  "cardType": "VISA",
  "acquirerReference": "wp_4f8a21c7",
  "status": "CHARGED"
}
```

## Contract stability

This response is consumed outside this service. Treat it as versioned even though there is no
version in the path.

| Field | Consumer | Used for |
| --- | --- | --- |
| `cardType` | `order-service` | branches on the network to set order state and choose the receipt template |
| `cardType` | finance reconciliation | groups the daily settlement file by network |
| `acquirerReference` | finance reconciliation, refunds | matching our charges to the acquirer's settlement report |
| `total`, `status` | `order-service` | order total and whether to release the order |

### `cardType` values

`VISA` or `MASTERCARD`. Nothing else is currently emitted.

**Adding a value is a change consumers must be told about.** `order-service` branches on this
field, and an unrecognised value falls through to its default path rather than failing loudly.

**Changing what the field means is worse.** The name and JSON type stay the same, so nothing
fails validation — the data simply arrives wrong, and reconciliation groups money under the
wrong heading. If new information needs to be carried, add a new field and leave `cardType`
alone.

## Errors

| Status | When |
| --- | --- |
| `400` | card number missing or the network is not one we accept |
| `404` | invoice not found |
| `502` | `tax-service` could not price the tax — we do not charge an untaxed amount |
