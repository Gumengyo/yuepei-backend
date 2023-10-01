package com.gumeng.usercenter.model.request;

import lombok.Data;

/**
 * @author 顾梦
 * @description 修改用户
 * @since 2023/8/5
 */
@Data
public class UserUpdateRequest {


    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 校验密码
     */
    private String checkPassword;

}
