package com.latto.chronos.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class EventType implements Serializable {
    private int id;
    private String name;
    private String code;
    private String description;

    @SerializedName("has_duration")
    private int hasDuration; // 0 or 1

    @SerializedName("requires_pastor")
    private int requiresPastor;

    private String icon; // e.g. "ic_event_meeting" (drawable name)

    @SerializedName("color_hex")
    private String colorHex; // e.g. "#FF9800"

    // Getters / Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getHasDuration() { return hasDuration; }
    public void setHasDuration(int hasDuration) { this.hasDuration = hasDuration; }

    public int getRequiresPastor() { return requiresPastor; }
    public void setRequiresPastor(int requiresPastor) { this.requiresPastor = requiresPastor; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
}
