package com.gumeng.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 顾梦
 * @description 评论包装类
 * @since 2023/8/7
 */
@Data
public class CommentsVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 关联的1级评论id，如果是一级评论，则值为0
     */
    private Long parentId;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 文章id
     */
    private Long blogId;

    /**
     * 回复的内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 评论的图片
     */
    private String contentImg;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 评论用户信息
     */
    private CommentUserVO user;

    /**
     * 回复的评论
     */
    private CommentReplyVO reply;

    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = -1461567317259590205L;

}
