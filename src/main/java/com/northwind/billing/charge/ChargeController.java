package com.northwind.billing.charge;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Charges an invoice. In production since 2024 — every call here moves money.
 */
@RestController
@RequestMapping("/v1/invoices")
public class ChargeController {

    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }

    @PostMapping("/{invoiceId}/charge")
    public ChargeResponse charge(@PathVariable("invoiceId") String invoiceId,
                                 @Valid @RequestBody ChargeRequest request) {
        return chargeService.charge(invoiceId, request);
    }
}
