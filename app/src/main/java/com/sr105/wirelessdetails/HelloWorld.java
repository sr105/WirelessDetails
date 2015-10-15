package com.sr105.wirelessdetails;

public class HelloWorld {
    public static void main(String[] args) {
        String name = "";
        if (args.length > 0)
            name = args[0];
        System.out.println("Hello World! " + name); // Display the string.
    }
}
