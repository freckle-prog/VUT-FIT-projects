package com.example.demo;

// Reprezentuje operaci //

import java.util.ArrayList;
import java.util.List;

public class UMLOperation extends Element{
    public String op_name;
    public String return_type;
    public List input_params;

    //Constructor
    public UMLOperation(String name, String return_t) {
        super(name);
        this.op_name = name;
        this.return_type = return_t;
        this.input_params = new ArrayList<>();
    }

    public String getOp_name (){
        return this.op_name;
    }

//    public static UMLOperation create(java.lang.String name, String type, UMLAttribute... args) {
//        UMLOperation umlOperation = new UMLOperation(name, type);
//        umlOperation.list_operations.addAll(Arrays.asList(args));
//        return umlOperation;
//    }

    public void addInput(String arg) {
        for (Object input_param : this.input_params) {
            if (input_param.equals(arg)) {
                return;
            }
        }
        this.input_params.add(arg);
    }
//
//    public java.util.List<UMLAttribute> getArguments() {
//        return Collections.unmodifiableList(this.list_operations);
//    }

    public java.lang.String toString() {
        return "Operation name: " + this.op_name + "\n" + "input: " + this.input_params + " return: " + return_type;
    }
}

