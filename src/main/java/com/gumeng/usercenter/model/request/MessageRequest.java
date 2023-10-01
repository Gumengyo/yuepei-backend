package com.gumeng.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 顾梦
 * @description 消息请求
 * @since 2023/8/17
 */
@Data
public class MessageRequest implements Serializable {
    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = 1324635911327892058L;
    /**
     * 为id
     */
    private Long toId;
    /**
     * 团队id
     */
    private Long teamId;
    /**
     * 文本
     */
    private String text;
    /**
     * 聊天类型
     */
    private Integer chatType;
    /**
     * 是管理
     */
    private boolean isAdmin;
}
