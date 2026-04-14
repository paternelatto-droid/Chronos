package com.latto.chronos.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "church_app_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TOKEN_EXPIRATION = "token_expiration";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Sauvegarde la session de l'utilisateur
     */
    public void saveSession(int userId, String username, String token,long token_expires_in) {
        // Stockage du token et de l'expiration
        long now = System.currentTimeMillis(); // ms
        long expInMs = now + (token_expires_in * 1000L); // convertir secondes → ms

        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_TOKEN, token);
        editor.putLong(KEY_TOKEN_EXPIRATION, expInMs);
        editor.apply();
    }

    /**
     * Vérifie si l'utilisateur est connecté
     */
    public boolean isLoggedIn() {
        return sharedPreferences.contains(KEY_USER_ID) && sharedPreferences.contains(KEY_TOKEN);
    }

    public boolean isTokenValid() {
        long expiration = sharedPreferences.getLong("token_expiration", 0);
        return System.currentTimeMillis() < expiration;
    }

    /**
     * Récupère l'ID utilisateur
     */
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, 0);
    }

    /**
     * Récupère le nom d'utilisateur
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    /**
     * Récupère le token
     */
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    /**
     * Supprime la session (déconnexion)
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
