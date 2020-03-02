package com.sun.designPattern.adapter;

/**
 * 需要适配的类
 * 一般情况下，Adaptee 类中有真正的业务逻辑，但是其接口不能被调用者直接使用
 */
public class Adaptee {
    public String sayHello(String name){
        return String.format("Hello:%s", name);
    }
}
