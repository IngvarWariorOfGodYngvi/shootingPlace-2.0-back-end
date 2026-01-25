package com.shootingplace.shootingplace.posnet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

@Component
public class NonFiscalReceiptListener {

    private static final Logger LOG =
            LoggerFactory.getLogger(NonFiscalReceiptListener.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NonFiscalReceiptRequestedEvent event) {

        LOG.info("AFTER_COMMIT → sending to posnet-bridge: {}",
                event.getPluQuantityMap());

        NonFiscalPrintRequest request =
                new NonFiscalPrintRequest(event.getPluQuantityMap());

        try {
            restTemplate.postForEntity(
                    "http://localhost:9123/print/non-fiscal",
                    request,
                    Void.class
            );
        } catch (Exception e) {
            // BEST EFFORT – NIE cofamy transakcji
            LOG.error("Failed to send receipt to posnet-bridge", e);
        }
    }
}
