package com.gumeng.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评论邮件
 * @TableName comment_email
 */
@TableName(value ="comment_email")
@Data
public class CommentEmail implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收件人头像
     */
    private String avatar;

    /**
     * 邮件主题
     */
    private String subject;

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

    /**
     * 失败次数
     */
    private Integer failNum;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}