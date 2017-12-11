package com.ft.platform.dropwizard;

import java.util.Objects;

public class GoodToGoResult {
     private boolean goodToGo;
     private String errorMessage;

    public GoodToGoResult(boolean goodToGo, String errorMessage) {
        this.goodToGo = goodToGo;
        this.errorMessage = errorMessage;
    }

    public boolean isGoodToGo() {
        return goodToGo;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoodToGoResult that = (GoodToGoResult) o;
        return goodToGo == that.goodToGo &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(goodToGo, errorMessage);
    }
}
