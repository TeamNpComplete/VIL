package com.vil.vil_bot.models;

public class RechargeDetails {
    int price;
    String rechargeType;
    String rechargeLimit;
    String rechargeUsage;
    String rechargeValidity;

    public RechargeDetails(int price, String rechargeType, String rechargeLimit, String rechargeUsage, String rechargeValidity) {
        this.price = price;
        this.rechargeType = rechargeType;
        this.rechargeLimit = rechargeLimit;
        this.rechargeUsage = rechargeUsage;
        this.rechargeValidity = rechargeValidity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getRechargeType() {
        return rechargeType;
    }

    public void setRechargeType(String rechargeType) {
        this.rechargeType = rechargeType;
    }

    public String getRechargeLimit() {
        return rechargeLimit;
    }

    public void setRechargeLimit(String rechargeLimit) {
        this.rechargeLimit = rechargeLimit;
    }

    public String getRechargeUsage() {
        return rechargeUsage;
    }

    public void setRechargeUsage(String rechargeUsage) {
        this.rechargeUsage = rechargeUsage;
    }

    public String getRechargeValidity() {
        return rechargeValidity;
    }

    public void setRechargeValidity(String rechargeValidity) {
        this.rechargeValidity = rechargeValidity;
    }
}
