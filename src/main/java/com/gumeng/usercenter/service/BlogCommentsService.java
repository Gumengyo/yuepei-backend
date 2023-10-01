package com.gumeng.usercenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.usercenter.model.domain.BlogComments;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.request.CommentAddRequest;
import com.gumeng.usercenter.model.vo.BlogCommentsVO;
import com.gumeng.usercenter.model.vo.CommentUserVO;
import com.gumeng.usercenter.model.vo.CommentsVO;

import java.util.List;

/**
 * @author 顾梦
 * @description 针对表【blog_comments(文章评论表)】的数据库操作Service
 * @createDate 2023-08-06 16:00:31
 */
public interface BlogCommentsService extends IService<BlogComments> {

    /**
     * 根据文章id获取评论
     *
     * @param blogId
     * @return
     */
    List<CommentsVO> getCommentsByBlogId(Long blogId);

    /**
     * 用户点赞评论
     *
     * @param id
     * @param userId
     * @return
     */
    boolean likeComment(Long id, Long userId);

    /**
     * 添加评论
     *
     * @param commentAddRequest
     * @param loginUser
     * @return
     */
    CommentsVO addComment(CommentAddRequest commentAddRequest, User loginUser);

    /**
     * 根据id获取评论信息
     *
     * @param id
     * @return
     */
    CommentsVO getCommentsById(Long id);

    /**
     * 获取评论用户信息
     *
     * @param loginUser
     * @return
     */
    CommentUserVO getCommentUser(Long blogId, User loginUser);

    /**
     * 删除评论
     *
     * @param comments
     * @param blogId
     * @param loginUser
     * @return
     */
    boolean deleteComments(CommentsVO comments, Long blogId, User loginUser);

    /**
     * 查询与我有关的评论
     * @param userId
     * @return
     */
    List<BlogCommentsVO> getMyComments(Long userId);

}
