package com.latto.chronos.response;

public class AvailabilityResponse {
    private boolean success;
    private boolean available;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getMessage() {
        return message;
    }
}


