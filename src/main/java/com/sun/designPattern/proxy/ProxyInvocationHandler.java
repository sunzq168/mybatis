package com.sun.designPattern.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyInvocationHandler implements InvocationHandler {
    private Object target;

    public ProxyInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(args[0] + "来了");
        Object result = method.invoke(target, args);
        System.out.println(args[0] + "再见");

        return result;
    }
}
