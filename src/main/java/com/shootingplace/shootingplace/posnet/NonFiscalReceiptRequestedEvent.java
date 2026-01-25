package com.shootingplace.shootingplace.posnet;

import java.util.Map;

public class NonFiscalReceiptRequestedEvent {

    private final Map<Integer, Integer> pluQuantityMap;

    public NonFiscalReceiptRequestedEvent(Map<Integer, Integer> pluQuantityMap) {
        this.pluQuantityMap = pluQuantityMap;
    }

    public Map<Integer, Integer> getPluQuantityMap() {
        return pluQuantityMap;
    }
}
