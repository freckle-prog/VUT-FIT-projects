package com.example.demo;

import java.util.ArrayList;
import java.util.List;


public class JsonDiagram{
    public java.lang.String diagram_name;
    public List <UMLClass> diagram_classes;

    public JsonDiagram(String name) {
        this.diagram_name = name;
        this.diagram_classes = new ArrayList<>();
    }

    public UMLClass createClass(java.lang.String name) {
        //Vytvoří instanci UML třídy a vloží ji do diagramu.
        //Pokud v diagramu již existuje třída stejného názvu, nedělá nic.
        for (UMLClass umlClass : this.diagram_classes) {
            if (umlClass.getClass_name().equals(name)) {
                return null;
            }
        }
        UMLClass umlClass = new UMLClass(name);
        this.diagram_classes.add(umlClass);
        return umlClass;
    }


}
