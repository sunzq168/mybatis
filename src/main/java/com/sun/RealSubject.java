package com.sun;

public class RealSubject implements Subject {
    @Override
    public void hello(String name) {
        System.out.println("hello: " + name);
    }
}
