package com.sun.typeResolver;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.reflection.TypeParameterResolver;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TestType {
    SubClassA<Long> sa = new SubClassA<>();

    public static void main(String[] args) throws Exception {
        Field f = A.class.getDeclaredField("map");
        System.out.println(f.getGenericType());
        System.out.println(f.getGenericType() instanceof ParameterizedType);

        Type type = TypeParameterResolver.resolveFieldType(f, ParameterizedTypeImpl.make(SubClassA.class, new
                Type[]{Long.class}, TestType.class));
        System.out.println(type.getClass());

        ParameterizedType p = (ParameterizedType) type;
        System.out.println(p.getRawType());
        System.out.println(p.getOwnerType());
        for (Type t : p.getActualTypeArguments()) {
            System.out.println(t);
        }

    }
}
