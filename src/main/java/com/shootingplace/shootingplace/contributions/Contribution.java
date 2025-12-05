package com.shootingplace.shootingplace.contributions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Contribution {

    private LocalDate paymentDay;
    private LocalDate validThru;
    private String historyUUID;

    public LocalDate getPaymentDay() {
        return paymentDay;
    }

    public void setPaymentDay(LocalDate paymentDay) {
        this.paymentDay = paymentDay;
    }

    public LocalDate getValidThru() {
        return validThru;
    }

    public void setValidThru(LocalDate validThru) {
        this.validThru = validThru;
    }

    public String getHistoryUUID() {
        return historyUUID;
    }

    public void setHistoryUUID(String historyUUID) {
        this.historyUUID = historyUUID;
    }
}
