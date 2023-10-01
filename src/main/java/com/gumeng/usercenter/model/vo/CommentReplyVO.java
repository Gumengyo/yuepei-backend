package com.gumeng.usercenter.model.vo;

import com.gumeng.usercenter.model.dto.Comments;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 顾梦
 * @description 评论回复
 * @since 2023/8/8
 */
@Data
public class CommentReplyVO implements Serializable {

    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = -1461567317259590205L;

    /**
     * 回复的评论数
     */
    int total;

    /**
     * 回复的评论
     */
    List<Comments> list;

}
