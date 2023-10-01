package com.gumeng.usercenter.model.vo;

import com.google.gson.annotations.Expose;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 顾梦
 * @description 聊天信息vo
 * @since 2023/8/17
 */
@Data
public class ChatMessageVO implements Serializable {

    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = -4722378360550337925L;

    /**
     * 发送用户id
     */
    private Long fromId;

    /**
     * 接收用户id
     */
    private Long toId;

    /**
     * 来自用户
     */
    private ChatUserVO fromUser;

    /**
     * 团队id
     */
    private Long teamId;

    /**
     * 队伍封面
     */
    private TeamVO team;

    /**
     * 文本
     */
    private String text;


    /**
     * 聊天类型
     */
    private Integer chatType;

    /**
     * 未读消息数
     */
    private Long messageNum;

    /**
     * 是管理
     */
    private Boolean isAdmin = false;

    /**
     * 创建时间
     */
    private String createTime;
}
