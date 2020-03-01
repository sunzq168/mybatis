package com.sun.designPattern.adapter;

/**
 * 适配器
 * Adapter 实现了 Target 接 口，并包装了一个 Adaptee 对象。
 * Adapter 在实现 Target接口中的方法时，会将调用委托给 Adaptee对象的相关方法，由 Adaptee 完成具体的业务。
 */
public class Adapter implements Target{
    private Adaptee adaptee;

    public Adapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public String sayHello(String name) {
        return adaptee.sayHello(name);
    }
}
