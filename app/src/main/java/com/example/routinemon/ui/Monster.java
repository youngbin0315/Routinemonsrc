package com.example.routinemon.ui;

public class Monster {
    public String name;
    public int imageRes;
    public boolean isUnlocked;

    public Monster(String name, int imageRes, boolean isUnlocked) {
        this.name = name;
        this.imageRes = imageRes;
        this.isUnlocked = isUnlocked;
    }
}