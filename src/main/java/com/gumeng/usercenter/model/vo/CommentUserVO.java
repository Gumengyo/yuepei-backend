package com.gumeng.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 顾梦
 * @description 评论用户包装类
 * @since 2023/8/8
 */
@Data
public class CommentUserVO implements Serializable {
    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = -1461567317259590205L;

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 级别
     */
    private Integer level;

    /**
     * 点赞过的的评论id
     */
    List<Long> likeIds;

    /**
     * 用户主页
     */
    private String homeLink;
}
