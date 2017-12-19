package com.ravi.android.buddy.ledger.model;

import java.util.List;

/**
 * Created by ravi on 7/1/17.
 */

public class User {
    private long id;
    private String name;
    private String number;
    private String email;
    private String creationDate;
    private String userImageLocation;

    private Double monthlyBalanceAmount = 0D;

    public Double getMonthlyBalanceAmount() {
        return monthlyBalanceAmount;
    }

    public void setMonthlyBalanceAmount(Double monthlyBalanceAmount) {
        this.monthlyBalanceAmount = monthlyBalanceAmount;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    private String gender;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getUserImageLocation() {
        return userImageLocation;
    }

    public void setUserImageLocation(String userImageLocation) {
        this.userImageLocation = userImageLocation;
    }
}
