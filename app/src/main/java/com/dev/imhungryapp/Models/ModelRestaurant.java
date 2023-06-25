package com.dev.imhungryapp.Models;

public class ModelRestaurant {
    private String email, address, phone, uid, timestamp, online;

    public ModelRestaurant() {
    }

    public ModelRestaurant(String email, String address, String phone, String uid, String timestamp, String online) {
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.uid = uid;
        this.timestamp = timestamp;
        this.online = online;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }
}
