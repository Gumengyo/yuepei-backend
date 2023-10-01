package com.gumeng.usercenter.controller;


import com.gumeng.usercenter.common.BaseResponse;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.common.ResultUtils;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.mapper.BlogCommentsMapper;
import com.gumeng.usercenter.model.domain.BlogComments;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.request.CommentAddRequest;
import com.gumeng.usercenter.model.request.UserLoginRequest;
import com.gumeng.usercenter.model.vo.BlogCommentsVO;
import com.gumeng.usercenter.model.vo.CommentUserVO;
import com.gumeng.usercenter.model.vo.CommentsVO;
import com.gumeng.usercenter.service.BlogCommentsService;
import com.gumeng.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 顾梦
 * @description 评论管理接口
 * @since 2023/8/6
 */
@RestController
@RequestMapping("/comments")
@Api(tags = "评论管理接口")
public class CommentsController {

    @Resource
    BlogCommentsService blogCommentsService;

    @Resource
    UserService userService;

    /**
     * 根据博客id获取评论列表
     * @param blogId
     * @return
     */
    @ApiOperation(value = "根据博客id获取评论列表")
    @GetMapping
    public BaseResponse<List<CommentsVO>> getCommentsByBlogId(Long blogId) {

        if (blogId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<CommentsVO> commentsVOList = blogCommentsService.getCommentsByBlogId(blogId);

        return ResultUtils.success(commentsVOList);
    }

    /**
     * 添加评论
     * @param commentAddRequest
     * @param request
     * @return
     */
    @ApiOperation(value = "添加评论")
    @PostMapping("/add")
    public BaseResponse<CommentsVO> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {

        if (commentAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        CommentsVO commentsVO = blogCommentsService.addComment(commentAddRequest, loginUser);
        return ResultUtils.success(commentsVO);
    }

    /**
     * 点赞评论
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "点赞评论")
    @PutMapping("/like/{id}")
    public BaseResponse<Boolean> likeComment(@PathVariable("id") Long id, HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        boolean liked = blogCommentsService.likeComment(id,userId);
        return ResultUtils.success(liked);
    }

    /**
     * 获取我的评论列表
     * @param request
     * @return
     */
    @ApiOperation(value = "获取我的评论列表")
    @GetMapping("/list/my")
    public BaseResponse<List<BlogCommentsVO>> listMyComments(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        List<BlogCommentsVO> blogCommentsVOList = blogCommentsService.getMyComments(userId);
        return ResultUtils.success(blogCommentsVOList);
    }

    /**
     * 根据评论id获取评论详情
     * @param id
     * @return
     */
    @ApiOperation(value = "根据评论id获取评论详情")
    @GetMapping("/{id}")
    public BaseResponse<CommentsVO> getCommentsById(@PathVariable("id") Long id) {

        CommentsVO commentsVO = blogCommentsService.getCommentsById(id);
        return ResultUtils.success(commentsVO);
    }

    /**
     * 获取评论用户
     * @param blogId
     * @param request
     * @return
     */
    @ApiOperation(value = "获取评论用户")
    @GetMapping("/user/{blogId}")
    public BaseResponse<CommentUserVO> getCommentUser(@PathVariable("blogId") Long blogId, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        CommentUserVO commentUser = blogCommentsService.getCommentUser(blogId,loginUser);
        return ResultUtils.success(commentUser);
    }

    /**
     * 删除评论
     * @param comments
     * @param blogId
     * @param request
     * @return
     */
    @ApiOperation(value = "删除评论")
    @PostMapping("/delete/{blogId}")
    public BaseResponse<Boolean> deleteComments(@RequestBody CommentsVO comments,@PathVariable("blogId") Long blogId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        if (comments == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean deleted = blogCommentsService.deleteComments(comments,blogId,loginUser);

        return ResultUtils.success(deleted);
    }
}

















