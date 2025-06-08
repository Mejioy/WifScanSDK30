package ru.kafpin.wifscansdk30;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

//@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ElementMini {
    private String title;
    private String level;

    public ElementMini(String title, String level) {
        this.title = title;
             this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public String getLevel() {
        return level;
    }
}
