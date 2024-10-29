package org.example.portfolio.model;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Portfolio {

    private String name;

    @JsonProperty("is_disabled")
    private boolean isDisabled;

    public Portfolio() {
    }

    public Portfolio(String name, boolean isDisabled) {
        this.name = name;
        this.isDisabled = isDisabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean isDisabled) {
        isDisabled = isDisabled;
    }
}
