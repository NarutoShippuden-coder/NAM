package org.example.portfolio.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Holding {


    @JsonProperty("stock_id")
    private String stockId;
    private double value;

    public Holding() {
    }

    public Holding(String stockId, double value) {
        this.stockId = stockId;
        this.value = value;
    }

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Holding)) {
            return false;
        }

        Holding h = (Holding) obj;
        return this.stockId.equals(h.stockId)
                && (Double.compare(this.value, h.value) == 0);
    }
}
