package com.gumeng.usercenter.model.dto;

import com.gumeng.usercenter.model.vo.BlogVO;
import com.gumeng.usercenter.model.vo.CommentsVO;
import com.gumeng.usercenter.model.vo.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 顾梦
 * @description 点赞消息
 * @since 2023/8/12
 */
@Data
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 点赞类型 - 0:文章 - 1:评论
     */
    private int type;

    /**
     * 点赞的用户
     */
    private UserVO fromUser;

    /**
     * 点赞时间
     */
    private LocalDateTime createTime;

    /**
     * 文章
     */
    private BlogVO blog;

    /**
     * 评论信息
     */
    private CommentsVO comment;

}
