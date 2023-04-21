package com.irlab.view.entity;

import java.io.Serializable;

public class Friend implements Serializable {

    Long id;

    String username;

    String level;

    Boolean online;

    Boolean carryOnline;

    Boolean webOnline;

    public Friend(Long id, String username, String level, Boolean online, Boolean carryOnline, Boolean webOnline) {
        this.id = id;
        this.username = username;
        this.level = level;
        this.online = online;
        this.carryOnline = carryOnline;
        this.webOnline = webOnline;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Boolean getCarryOnline() {
        return carryOnline;
    }

    public void setCarryOnline(Boolean carryOnline) {
        this.carryOnline = carryOnline;
    }

    public Boolean getWebOnline() {
        return webOnline;
    }

    public void setWebOnline(Boolean webOnline) {
        this.webOnline = webOnline;
    }
}
