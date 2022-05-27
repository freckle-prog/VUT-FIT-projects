package com.example.demo;

import java.util.ArrayList;
import java.util.List;
// Reprezentuje diagram trid //

public class ClassDiagram extends Element{
    public List<UMLClassifier> class_list;
    public java.lang.String diagram_name;
    public UMLClass diagram_class;
    //private int id;

    //Constructor
    public ClassDiagram(java.lang.String name) {
        super(name);
        this.diagram_name = name;
        this.class_list = new ArrayList<UMLClassifier>();
    }

    public UMLClass createClass(java.lang.String name) {
        //Vytvoří instanci UML třídy a vloží ji do diagramu.
        //Pokud v diagramu již existuje třída stejného názvu, nedělá nic.
        for (UMLClassifier umlClass : this.class_list) {
            if (umlClass.getName().equals(name)) {
                return null;
            }
        }
        UMLClass umlClass = new UMLClass(name);
        this.class_list.add(umlClass);
        return umlClass;
    }

    public UMLClassifier classifierForName(java.lang.String name) {
        //We are trying to find thing in the diagram with the attr_name,
        //if we won't => we will create UMLClassifier, that doesn't exist
        //in diagram.
        if (findClassifier(name) == null) {
            UMLClassifier umlClassifier = UMLClassifier.forName(name);
            this.class_list.add(umlClassifier);
        }
        return findClassifier(name);
    }

    public UMLClassifier findClassifier(java.lang.String name) {
        ClassDiagram diagram = new ClassDiagram(name);
        for (UMLClassifier umlClass : this.class_list) {
            if (umlClass.getName().equals(name)) {
                return  umlClass;
            }
        }
        return null;
    }
}
