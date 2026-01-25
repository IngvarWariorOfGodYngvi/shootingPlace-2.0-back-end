package com.shootingplace.shootingplace.posnet;

import java.util.Map;

public class NonFiscalPrintRequest {

    private Map<Integer, Integer> items;

    public NonFiscalPrintRequest(Map<Integer, Integer> items) {
        this.items = items;
    }

    public Map<Integer, Integer> getItems() {
        return items;
    }
}
