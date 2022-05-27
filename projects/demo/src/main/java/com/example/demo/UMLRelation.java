package com.example.demo;

public class UMLRelation {
    public String type;
    public String first_class;
    public String second_class;

    UMLRelation (String type, String first_class, String second_class) {
        this.type = type;
        this.first_class = first_class;
        this.second_class = second_class;
    }

    public boolean type_check (String type) {
        switch (type) {
            case "association":
            case "dependency":
            case "aggregation":
            case "composition":
            case "implementation":
            case "inheritance":
                return true;
            default:
                return false;
        }
    }
}
