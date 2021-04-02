package com.supportportal.Security.domain;

public class Greeter {

    public String greet(String name){
        return "Hello " + capitalizeFirstLetter(name.trim());
    }

    public String capitalizeFirstLetter(String string){
        String firstLetter = string.substring(0, 1);
        String resto = string.substring(1);

        return firstLetter.toUpperCase() + resto;
    }

    public Greeter() {
    }

}
