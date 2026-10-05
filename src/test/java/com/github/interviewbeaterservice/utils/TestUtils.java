package com.github.interviewbeaterservice.utils;

import com.github.interviewbeaterservice.user.entity.Role;
import com.github.interviewbeaterservice.user.entity.User;

import java.lang.reflect.Field;

public class TestUtils {

    /**
     * Создает дефолтного пользователя
     * @param user пользователь
     * @param id id пользователя
     * @param role роль пользователя
     * @return пользователь
     */
    public static User withId(User user, Long id, Role role) {
        setField(user, "id", id);
        setField(user, "role", role);
        return user;
    }

    /**
     * Изменение поля у объекта
     * @param target объект
     * @param name название поля
     * @param value значение, которое хотим проставить в поле
     */
    public static void setField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
