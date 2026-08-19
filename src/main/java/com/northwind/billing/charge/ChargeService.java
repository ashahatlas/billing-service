package com.northwind.billing.charge;

import com.northwind.billing.acquirer.AcquirerClient;
import com.northwind.billing.card.CardNetwork;
import com.northwind.billing.client.TaxServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class ChargeService {

    private static final Logger log = LoggerFactory.getLogger(ChargeService.class);

    private final AcquirerClient acquirerClient;
    private final TaxServiceClient taxServiceClient;
    private final InvoiceRepository invoiceRepository;

    public ChargeService(AcquirerClient acquirerClient,
                         TaxServiceClient taxServiceClient,
                         InvoiceRepository invoiceRepository) {
        this.acquirerClient = acquirerClient;
        this.taxServiceClient = taxServiceClient;
        this.invoiceRepository = invoiceRepository;
    }

    public ChargeResponse charge(String invoiceId, ChargeRequest request) {
        Invoice invoice = invoiceRepository.require(invoiceId);
        CardNetwork network = CardNetwork.fromBin(request.cardNumber());

        if (!acquirerClient.supports(network)) {
            throw new IllegalArgumentException("we do not accept " + network + " yet");
        }

        BigDecimal subtotal = invoice.subtotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = taxServiceClient
                .taxFor(invoiceId, subtotal, request.currency(), request.billingPostcode())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);

        AcquirerClient.AcquirerResult result =
                acquirerClient.charge(invoiceId, total, request.currency(), network);

        String chargeId = "chg_" + UUID.randomUUID();
        log.info("charged invoiceId={} chargeId={} network={} total={} {}",
                invoiceId, chargeId, network, total, request.currency());

        return new ChargeResponse(
                chargeId,
                invoiceId,
                subtotal,
                tax,
                total,
                request.currency(),
                network.code(),
                result.acquirerReference(),
                result.status());
    }
}
