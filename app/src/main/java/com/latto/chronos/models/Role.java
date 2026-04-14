package com.latto.chronos.models;

public class Role {

    private int id;
    private String name;
    private String description;

    public Role(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Obligatoire pour Retrofit / Gson
    public Role() {}

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Pour afficher automatiquement le nom dans le Spinner
    @Override
    public String toString() {
        return name;
    }
}

