package com.ravi.android.buddy.ledger.model;

import java.util.Date;

/**
 * Created by ravi on 7/1/17.
 */

public class UserTransaction {

    private long id;

    private long userId;

    private String description;

    private String transaction_date;

    private String transaction_type;

    private Double amount;

    private int MonthTransactionIndex;

    public int getMonthTransactionIndex() {
        return MonthTransactionIndex;
    }

    public void setMonthTransactionIndex(int monthTransactionIndex) {
        MonthTransactionIndex = monthTransactionIndex;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(String transaction_date) {
        this.transaction_date = transaction_date;
    }

    public String getTransaction_type() {
        return transaction_type;
    }

    public void setTransaction_type(String transaction_type) {
        this.transaction_type = transaction_type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
