package com.gumeng.usercenter.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 顾梦
 * @description 评论回复邮件封装类
 * @since 2023/8/10
 */
@Data
public class CommentEmailInfo implements Serializable {

    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = -1461567317259590205L;

    /**
     * 收件人头像
     */
    private String avatar;

    /**
     * 收件人邮箱
     */
    private String email;

    /**
     * 收件人名字
     */
    private String parentNick;

    /**
     * 原评论内容
     */
    private String parentComment;


    /**
     * 回复人名字
     */
    private String nick;

    /**
     * 回复评论内容
     */
    private String comment;

    /**
     * 文章名字
     */
    private String postName;

    /**
     * 文章地址
     */
    private String postUrl;


}
