package com.sun;

public class ServerMain {
    public static void main(String[] args) {
        Subject subject = new RealSubject();
        ProxyHandler proxyHandler = new ProxyHandler(subject);
        Subject proxy = (Subject) proxyHandler.getProxyInstance();
        proxy.hello("world");

    }
}
