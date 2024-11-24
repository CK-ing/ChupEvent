package com.example.chupevent;

public class Program {
    private String title;
    private String subtitle;

    // Constructor
    public Program(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    // Getter for title
    public String getTitle() {
        return title;
    }

    // Getter for subtitle
    public String getSubtitle() {
        return subtitle;
    }

    // Optional setter methods, if you need to modify the data later
    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
