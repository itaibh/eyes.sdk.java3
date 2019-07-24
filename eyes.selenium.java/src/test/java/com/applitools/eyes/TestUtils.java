package com.applitools.eyes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class TestUtils {
    public static void setFinalStatic(java.lang.Class klass, String fieldName, Object newValue) throws Exception {
        Field field = klass.getDeclaredField(fieldName);
        setFinalStatic(field, newValue);
    }

    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
