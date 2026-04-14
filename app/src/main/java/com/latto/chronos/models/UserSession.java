package com.latto.chronos.models;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserSession {

    private static List<String> permissions = new ArrayList<>();
    private static final String PREF_NAME = "church_app_session";
    private static final String KEY_PERMISSIONS = "user_permissions";

    public static void setPermissions(Context context, List<String> perms) {
        permissions.clear();
        if (perms != null) {
            permissions.addAll(perms);
        }
        savePermissions(context);
    }

    public static boolean hasPermission(String key) {
        return permissions.contains(key);
    }

    public static void loadPermissions(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PERMISSIONS, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>() {}.getType();
            permissions = gson.fromJson(json, type);
        }
    }

    private static void savePermissions(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(permissions);
        editor.putString(KEY_PERMISSIONS, json);
        editor.apply();
    }

    public static void clearPermissions(Context context) {
        permissions.clear();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_PERMISSIONS).apply();
    }
}
