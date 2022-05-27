package com.example.demo;

public class JsonSequence {
    public String sequence_name;
    public int lifetime; // Y line in diagram
    public String msg; //Message
    public UMLClass involved_class;
    public UMLClass send_class;

    public JsonSequence (String sequence_name, int lifetime, String msg, int x) {
        this.sequence_name = sequence_name;
        this.lifetime = lifetime;
        this.msg = msg;
    }

    public JsonSequence (String name, int lifetime, double x) {
        this.send_class = new UMLClass(name);
        this.involved_class = new UMLClass(name);
        this.involved_class.positionX = x;
        this.send_class.positionX = x;
        this.lifetime = lifetime;
    }


    public JsonSequence (String seq, String name_from, String name_to, double posX_from, double posX_to, int lifetime_from, String message) {
        this.sequence_name = seq;
        this.send_class = new UMLClass(name_from);
        this.send_class.positionX = posX_from;
        this.lifetime = lifetime_from;
        this.involved_class = new UMLClass(name_to);
        this.involved_class.positionX = posX_to;
        this.msg = message;
    }

    public String toString () {
        return this.sequence_name + " " + this.msg + " from " + this.send_class.class_name + " to " + this.involved_class.getClass_name();
    }
}
