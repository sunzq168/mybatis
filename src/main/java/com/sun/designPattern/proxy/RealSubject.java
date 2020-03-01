package com.sun.designPattern.proxy;


public class RealSubject implements Subject {
    @Override
    public void sayHello(String name) {
        System.out.println(String.format("Hello:%s", name));
    }
}
