package com.latto.chronos.response;

import com.google.gson.annotations.SerializedName;

public class MemberWithUserGetResponse {

    private boolean success;

    private Member member;

    private User user;

    private String message;

    // ✅ Getters
    public boolean isSuccess() {
        return success;
    }

    public Member getMember() {
        return member;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    // ✅ Classes internes pour mapping JSON
    public static class Member {
        private int id;
        @SerializedName("first_name")
        private String firstName;
        @SerializedName("last_name")
        private String lastName;
        private String gender;
        private String phone;
        private String email;
        private String address;
        @SerializedName("date_of_birth")
        private String dateOfBirth;
        @SerializedName("baptism_date")
        private String baptismDate;
        private String status;

        public int getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getGender() { return gender; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getAddress() { return address; }
        public String getDateOfBirth() { return dateOfBirth; }
        public String getBaptismDate() { return baptismDate; }
        public String getStatus() { return status; }
    }

    public static class User {
        private int id;
        private String name;
        private String username;
        private String email;
        @SerializedName("role_id")
        private int roleId;
        @SerializedName("is_active")
        private int isActive;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public int getRoleId() { return roleId; }
        public int isActive() { return isActive; }
    }
}
