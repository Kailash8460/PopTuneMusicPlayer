package com.example.poptunemusicplayer;

import java.io.Serializable;

public class ItemModel implements Serializable {
    private String name;

    public ItemModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
