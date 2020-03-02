package com.sun.designPattern.adapter;

public class AdapterMain {
    public static void main(String[] args) {
        Adaptee adaptee = new Adaptee();
        Target target = new Adapter(adaptee);
        String result = target.sayHello("sunzheng");
        System.out.println(result);
    }
}
