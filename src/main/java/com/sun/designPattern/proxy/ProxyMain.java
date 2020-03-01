package com.sun.designPattern.proxy;

import java.lang.reflect.Proxy;

public class ProxyMain {
    public static void main(String[] args) {
        RealSubject realSubject = new RealSubject();
        Subject proxy = (Subject) Proxy.newProxyInstance(
                RealSubject.class.getClassLoader(),RealSubject.class.getInterfaces(),new ProxyInvocationHandler(realSubject)
        );
        proxy.sayHello("sunzheng");
    }
}
