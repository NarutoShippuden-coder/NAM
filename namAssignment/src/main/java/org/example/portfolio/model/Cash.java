package org.example.portfolio.model;

public class Cash {

    private Double value;

    public Cash() {
    }

    public Cash(Double value) {
        this.value = value;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Cash)) {
            return false;
        }

        Cash c = (Cash) obj;
        return (Double.compare(this.value, c.value) == 0);
    }
}
