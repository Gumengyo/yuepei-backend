package com.gumeng.usercenter.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.usercenter.model.domain.BlogComments;
import com.gumeng.usercenter.model.dto.Comments;
import com.gumeng.usercenter.model.dto.CommentEmailInfo;
import com.gumeng.usercenter.model.vo.BlogCommentsVO;

import java.util.List;

/**
* @author 顾梦
* @description 针对表【blog_comments(文章评论表)】的数据库操作Mapper
* @createDate 2023-08-06 16:00:31
* @Entity generator.domain.BlogComments
*/
public interface BlogCommentsMapper extends BaseMapper<BlogComments> {

    /**
     * 根据文章di 获取评论
     * @param blogId
     * @return
     */
    List<Comments> getCommentsByBlogId(Long blogId);

    /**
     * 根据 id 获取评论
     * @param id
     * @return
     */
    Comments getCommentsById(Long id);

    /**
     * 根据id获取评论回复邮件信息
     * @param id
     * @return
     */
    CommentEmailInfo getComentEmailInfo(Long id);

    /**
     * 获取关于我的评论
     * @param userId
     * @return
     */
    List<BlogCommentsVO> getMyComments(Long userId);
}




