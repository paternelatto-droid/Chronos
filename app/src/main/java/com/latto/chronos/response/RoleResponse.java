package com.latto.chronos.response;

import com.latto.chronos.models.Role;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RoleResponse {

    private boolean success;

    @SerializedName("roles")
    private List<Role> roles;

    public boolean isSuccess() {
        return success;
    }

    public List<Role> getRoles() {
        return roles;
    }
}
