package com.gumeng.usercenter.model.vo;

import com.gumeng.usercenter.model.dto.Comments;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 顾梦
 * @description 文章和评论信息封装类
 * @since 2023/8/11
 */
@Data
public class BlogCommentsVO implements Serializable {
    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = -1461567317259590205L;

    /**
     * 评论信息
     */
    private Comments comment;


    /**
     * 文章
     */
    private BlogVO blog;

    /**
     * 是否点赞
     */
    private Boolean isLike;

}
