package com.example.demo;

// Pojmenovany element, soucast jakekoliv casti diagramu //

public class Element extends Object {
    public String name;
    public int id;

    //Constructor
    Element(java.lang.String name) {
        this.name = name;
    }

    Element(java.lang.String name, int id) {
        this.name = name;
        this.id = id;
    }

    public java.lang.String getName() {
        String name = this.name;
        return name;
    }

    public void rename(java.lang.String newName) {
        this.name = newName;
    }
}

