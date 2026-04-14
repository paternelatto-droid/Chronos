package com.latto.chronos.models;

import java.io.Serializable;

public class MemberWithUserRequest implements Serializable {

    private int member_id;

    private String first_name;
    private String last_name;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private String date_of_birth;
    private String baptism_date;
    private String status;

    // User fields
    private boolean create_user;
    private String username; // optionnel, généré si vide
    private String password; // optionnel, généré si vide
    private int role_id;     // optionnel, par défaut 1
    int existingUserId;
    boolean isUserActive;
    public MemberWithUserRequest(String first_name, String last_name, String gender, String phone, String email,
                                 String address, String date_of_birth, String baptism_date, String status,
                                 boolean create_user, String username,String password, int role_id) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.date_of_birth = date_of_birth;
        this.baptism_date = baptism_date;
        this.status = status;
        this.create_user = create_user;
        this.username = username;
        this.password = password;
        this.role_id = role_id;
    }

    public MemberWithUserRequest() {

    }

    public MemberWithUserRequest(int memberId,String firstName, String lastName, String gender, String phone, String email, String address, String dob, String baptism, String status, boolean createUser, String username, String password, int selectedRoleId, int existingUserId, boolean isUserActive) {
        this.member_id = memberId;
        this.first_name = firstName;
        this.last_name = lastName;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.date_of_birth = dob;
        this.baptism_date = baptism;
        this.status = status;
        this.create_user = createUser;
        this.username = username;
        this.password = password;
        this.role_id = selectedRoleId;
        this.existingUserId = existingUserId;
        this.isUserActive = isUserActive;
    }

    // ✅ Getters et setters si besoin par Gson

    public int getMember_id() {
        return member_id;
    }

    public void setMember_id(int member_id) {
        this.member_id = member_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getBaptism_date() {
        return baptism_date;
    }

    public void setBaptism_date(String baptism_date) {
        this.baptism_date = baptism_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCreate_user() {
        return create_user;
    }

    public void setCreate_user(boolean create_user) {
        this.create_user = create_user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRole_id() {
        return role_id;
    }

    public void setRole_id(int role_id) {
        this.role_id = role_id;
    }
}
