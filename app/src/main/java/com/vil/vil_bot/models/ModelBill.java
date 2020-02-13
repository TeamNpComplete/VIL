package com.vil.vil_bot.models;

public class ModelBill {
    String recharge_amount,recharge_usage,recharge_validy;

    public ModelBill(String recharge_amount, String recharge_usage, String recharge_validy) {
        this.recharge_amount = recharge_amount;
        this.recharge_usage = recharge_usage;
        this.recharge_validy = recharge_validy;
    }

    public String getRecharge_amount() {
        return recharge_amount;
    }

    public void setRecharge_amount(String recharge_amount) {
        this.recharge_amount = recharge_amount;
    }

    public String getRecharge_usage() {
        return recharge_usage;
    }

    public void setRecharge_usage(String recharge_usage) {
        this.recharge_usage = recharge_usage;
    }

    public String getRecharge_validy() {
        return recharge_validy;
    }

    public void setRecharge_validy(String recharge_validy) {
        this.recharge_validy = recharge_validy;
    }
}
