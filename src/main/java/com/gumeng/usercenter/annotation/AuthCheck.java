package com.gumeng.usercenter.annotation;

import com.gumeng.usercenter.common.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验
 *
 * @author gumeng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 有任何一个角色
     *
     * @return
     */
    UserRole[] anyRole() default {};

    /**
     * 必须有某个角色
     *
     * @return
     */
    UserRole mustRole() default UserRole.USER;

}

