package com.latto.chronos.response;

import com.google.gson.annotations.SerializedName;

public class MemberWithUserResponse {

    private boolean success;

    @SerializedName("member_id")
    private int memberId;

    @SerializedName("user_created")
    private boolean userCreated;

    @SerializedName("generated_username")
    private String generatedUsername;

    @SerializedName("generated_password")
    private String generatedPassword;

    private String message;

    // ✅ Getters
    public boolean isSuccess() {
        return success;
    }

    public int getMemberId() {
        return memberId;
    }

    public boolean isUserCreated() {
        return userCreated;
    }

    public String getGeneratedUsername() {
        return generatedUsername;
    }

    public String getGeneratedPassword() {
        return generatedPassword;
    }

    public String getMessage() {
        return message;
    }

}
