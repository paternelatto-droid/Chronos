package com.latto.chronos.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Member implements Parcelable {
    private int id;
    private String first_name;
    private String last_name;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private String date_of_birth;
    private String baptism_date;
    private String status;
    private String created_at;
    private String updated_at;
    private String role_name;
    private int role_id;
    int user_id;
    protected Member(Parcel in) {
        id = in.readInt();
        first_name = in.readString();
        last_name = in.readString();
        gender = in.readString();
        phone = in.readString();
        email = in.readString();
        address = in.readString();
        date_of_birth = in.readString();
        baptism_date = in.readString();
        status = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
        role_name = in.readString();
        role_id = in.readInt();
        user_id = in.readInt();
    }

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    // ✅ Getters et Setters
    public int getId() { return id; }
    public String getFirst_name() { return first_name; }
    public String getLast_name() { return last_name; }

    public String getFullName() {
        String a = first_name != null ? first_name : "";
        String b = last_name != null ? last_name : "";
        return (a + " " + b).trim();
    }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getDate_of_birth() { return date_of_birth; }
    public String getBaptism_date() { return baptism_date; }
    public String getStatus() { return status; }
    public String getCreated_at() { return created_at; }
    public String getUpdated_at() { return updated_at; }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(first_name);
        parcel.writeString(last_name);
        parcel.writeString(gender);
        parcel.writeString(phone);
        parcel.writeString(email);
        parcel.writeString(address);
        parcel.writeString(date_of_birth);
        parcel.writeString(baptism_date);
        parcel.writeString(status);
        parcel.writeString(created_at);
        parcel.writeString(updated_at);
        parcel.writeString(role_name);
        parcel.writeInt(role_id);
        parcel.writeInt(user_id);
    }

    public String getRole_name() { return role_name; }
    public int getRole_id() { return role_id; }

    public int getUser_id() {
        return user_id;
    }
}
