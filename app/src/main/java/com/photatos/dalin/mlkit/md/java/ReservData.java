package com.photatos.dalin.mlkit.md.java;

public class ReservData {
    private String userName;
    private String message;

    public ReservData() { }

    public ReservData(String userName, String message) {
        this.userName = userName;
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
