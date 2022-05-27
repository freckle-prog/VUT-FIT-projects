package com.example.demo;

// Klasifikator v diagramu //

public class UMLClassifier extends Element{
    public boolean isUserDefined;
    public String classifier_name;

    //Constructor
    public UMLClassifier(java.lang.String name) {
        super (name);
        this.classifier_name = name;
        this.isUserDefined = false;
    }

    public UMLClassifier(java.lang.String name, boolean isUserDefined) {
        super(name);
        this.isUserDefined = isUserDefined;
    }

    public static UMLClassifier forName(java.lang.String name) {
        UMLClassifier umlClassifier = new UMLClassifier(name, false);
        return umlClassifier;
    }

    public boolean isUserDefined() {
        return isUserDefined;
    }

    public java.lang.String toString() {
        return this.name + "(" + this.isUserDefined + ")";
    }


}

