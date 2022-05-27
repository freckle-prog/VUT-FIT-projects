package com.example.demo;

// Atribut, ma jmeno a typ //

public class UMLAttribute extends Element{
    public String type;
    public String attr_name;

    //Constructor
    public UMLAttribute(String name, String type) {
        super(name);
        this.attr_name = name;
        this.type = type;
    }

    public String toString() {
        return this.attr_name + ":" + this.type;
    }
}

