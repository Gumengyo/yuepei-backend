package com.gumeng.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 顾梦
 * @description 聊天用户信息
 * @since 2023/8/17
 */
@Data
public class ChatUserVO implements Serializable {
    private static final long serialVersionUID = 4696612253320170315L;

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatarUrl;

}
