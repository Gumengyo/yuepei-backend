package com.gumeng.usercenter.controller;

import com.gumeng.usercenter.common.BaseResponse;
import com.gumeng.usercenter.common.ResultUtils;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.dto.Message;
import com.gumeng.usercenter.model.vo.BlogVO;
import com.gumeng.usercenter.service.BlogCommentsService;
import com.gumeng.usercenter.service.MessageService;
import com.gumeng.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 顾梦
 * @description 消息管理接口
 * @since 2023/8/11
 */
@RestController
@RequestMapping("/message")
@Api(tags = "消息管理接口")
@Slf4j
public class MessageController {

    @Resource
    UserService userService;


    @Resource
    MessageService messageService;

    /**
     * 获取评论数
     * @param request
     * @return
     */
    @ApiOperation(value = "获取评论数")
    @GetMapping("/comment/num")
    public BaseResponse<Long> getCommentNum(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long num = messageService.getCommentNum(userId);
        return ResultUtils.success(num);
    }

    /**
     * 获取点赞数
     * @param request
     * @return
     */
    @ApiOperation(value = "获取点赞数")
    @GetMapping("/like/num")
    public BaseResponse<Long> getLikeNum(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long num = messageService.getLikeNum(userId);
        return ResultUtils.success(num);
    }

    /**
     * 获取我收到的赞
     * @param request
     * @return
     */
    @ApiOperation(value = "获取我收到的赞")
    @GetMapping("/like")
    public BaseResponse<List<Message>> getMyLikeMessage(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<Message> myLikeMessage = messageService.getMyLikeMessage(loginUser);
        return ResultUtils.success(myLikeMessage);
    }

    /**
     * 获取未读私聊消息数
     * @param request
     * @return
     */
    @ApiOperation(value = "获取未读私聊消息数")
    @GetMapping("/chat/num")
    public BaseResponse<Long> getChatNum(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long num = messageService.getChatNum(userId);
        return ResultUtils.success(num);
    }

    /**
     * 获取与我相关消息数
     * @param request
     * @return
     */
    @ApiOperation(value = "获取与我相关消息数")
    @GetMapping("/about")
    public BaseResponse<Boolean> aboutMeMessageNum(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long chatNum = messageService.getChatNum(userId);
        Long likeNum = messageService.getLikeNum(userId);
        Long commentNum = messageService.getCommentNum(userId);
        if (chatNum != 0 || likeNum != 0 || commentNum != 0){
            return ResultUtils.success(true);
        }
        return ResultUtils.success(false);
    }

    /**
     * 获取动态消息数
     * @param request
     * @return
     */
    @ApiOperation(value = "获取动态消息数")
    @GetMapping
    public BaseResponse<Boolean> getAllMessageNum(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long chatNum = messageService.getChatNum(userId);
        Long likeNum = messageService.getLikeNum(userId);
        Long commentNum = messageService.getCommentNum(userId);
        Long followNum = messageService.getBlogOfFollowNum(userId);
        if (chatNum != 0 || likeNum != 0 || commentNum != 0 || followNum != 0){
            return ResultUtils.success(true);
        }
        return ResultUtils.success(false);
    }
}
