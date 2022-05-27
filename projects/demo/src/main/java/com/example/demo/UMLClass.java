package com.example.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UMLClass extends UMLClassifier {
    public String class_name;
    public String Abstract;
    public List <UMLAttribute> attributes;
    public List <UMLOperation> operations;
    public List <UMLRelation> relation_list;
    public List <JsonSequence> json_sequences;
    public double positionX;
    public double positionY;

    //Constructor
    public UMLClass(java.lang.String class_name) {
        super(class_name);
        this.class_name = class_name;
        this.Abstract = "false";
        this.attributes = new ArrayList<UMLAttribute>();
        this.operations = new ArrayList<UMLOperation>();
        this.relation_list = new ArrayList<UMLRelation>();
        this.json_sequences = new ArrayList<JsonSequence>();
    }

    public String getClass_name (){
        return this.class_name;
    }

    public boolean addRelation (UMLRelation relation) {
        for (UMLRelation umlRelation : this.relation_list) {
            if (umlRelation.equals(relation)) {
                return false;
            }
        }
        this.relation_list.add(relation);
        return true;
    }

    public boolean addAttribute(UMLAttribute attr) {
        for (UMLAttribute attribute : this.attributes) {
            if (attribute.equals(attr)) {
                return false;
            }
        }
        this.attributes.add(attr);
        return true;
    }

    public void removeAllAttributes () {
        for (int i = 0; i < this.attributes.size(); i++) {
            this.attributes.remove(i);
        }
    }

    public boolean addArgument(UMLOperation attr) {
        for (UMLOperation operation : this.operations) {
            if (operation.equals(attr)) {
                return false;
            }
        }
        this.operations.add(attr);
        return true;
    }

    public int getAttrPosition(UMLAttribute attr) {
        int counter = 0;
        for (UMLAttribute attribute : this.attributes) {
            if (attribute.equals(attr)) {
                return counter;
            }
            counter++;
        }
        return -1;
    }

    public int moveAttrAtPosition(UMLAttribute attr, int pos) {
        int old_pos = getAttrPosition(attr);
        if (old_pos == -1 || old_pos == pos) {
            return -1;
        }
        for (int i = pos; i <old_pos; i++) {
            UMLAttribute tmp = this.attributes.get(i);
            this.attributes.set(i, this.attributes.get(old_pos));
            this.attributes.set(old_pos, tmp);
        }
        return 0;
    }

    public List<UMLAttribute> getAttributes() {
        return Collections.unmodifiableList(this.attributes);
    }

    public List<UMLOperation> getArguments() {
        return Collections.unmodifiableList(this.operations);
    }

}

