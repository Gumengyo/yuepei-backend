package com.gumeng.usercenter.model.request;

import com.gumeng.usercenter.model.vo.CommentReplyVO;
import com.gumeng.usercenter.model.vo.CommentsVO;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * @author 顾梦
 * @description
 * @since 2023/8/7
 */
@Data
public class CommentAddRequest implements Serializable {

    /**
     * 文章 id
     */
    private Long blogId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论父id
     */
    private Long parentId;

    /**
     * 被回复的评论
     */
    private CommentsVO reply;

    /**
     * 上传的文件
     */
    private MultipartFile[] files;

}
