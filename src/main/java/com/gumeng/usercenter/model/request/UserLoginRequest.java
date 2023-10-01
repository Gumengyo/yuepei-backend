package com.gumeng.usercenter.model.request;


import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 *
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 验证码
     */
    private String code;



}
