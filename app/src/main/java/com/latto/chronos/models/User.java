package com.latto.chronos.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    private int id;

    private String name;

    private String email;

    @SerializedName("email_verified_at")
    private String emailVerifiedAt;

    private String username;

    private String password; // ⚠ Peut être laissé null côté Android si inutile

    @SerializedName("role_id")
    private int roleId;

    @SerializedName("member_id")
    private int memberId;

    @SerializedName("is_active")
    private int isActive; // 1 = actif, 0 = inactif

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;


    private List<String> permissions;

    // 🔹 GETTERS

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public String getUsername() {
        return username;
    }

    public int getRoleId() {
        return roleId;
    }

    public int getMemberId() {
        return memberId;
    }

    public int getIsActive() {
        return isActive;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // 🔹 UTILE → Vérifie si user est actif
    public boolean isActive() {
        return isActive == 1;
    }

    public List<String> getPermissions() { return permissions; }

    public void setPermissions(List<String> perms) {
        this.permissions = perms;
    }

}
