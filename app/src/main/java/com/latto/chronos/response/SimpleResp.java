package com.latto.chronos.response;

public class SimpleResp {
    public boolean success;
    public String message;
    private int id;

    public SimpleResp(boolean success, String message, int id) {
        this.success = success;
        this.message = message;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public boolean isSuccess() {
        return success;
    }
}
