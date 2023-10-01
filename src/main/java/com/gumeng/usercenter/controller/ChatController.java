package com.gumeng.usercenter.controller;

import com.gumeng.usercenter.common.BaseResponse;
import com.gumeng.usercenter.common.ResultUtils;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.dto.Choices;
import com.gumeng.usercenter.model.dto.GptMessage;
import com.gumeng.usercenter.model.vo.ChatMessageVO;
import com.gumeng.usercenter.model.vo.TeamVO;
import com.gumeng.usercenter.service.ChatService;
import com.gumeng.usercenter.service.TeamService;
import com.gumeng.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 顾梦
 * @description 聊天信息接口
 * @since 2023/8/17
 */
@RestController
@Slf4j
@Api(tags = "聊天信息接口")
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    /**
     * 获取私聊信息
     * @param toId
     * @param request
     * @return
     */
    @ApiOperation(value = "获取私聊信息")
    @GetMapping("/privateChat/{toId}")
    public BaseResponse<List<ChatMessageVO>> getPrivateChat(@PathVariable("toId") Long toId, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        List<ChatMessageVO> chatMessageVOS =chatService.getPrivateChat(userId,toId);
        return ResultUtils.success(chatMessageVOS);
    }

    /**
     * 获取队伍聊天信息
     * @param teamId
     * @param request
     * @return
     */
    @ApiOperation(value = "获取队伍聊天信息")
    @GetMapping("/teamChat/{teamId}")
    public BaseResponse<List<ChatMessageVO>> getTeamChat(@PathVariable("teamId") Long teamId, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        TeamVO teamDetail = teamService.getTeamDetail(teamId, userId);
        List<ChatMessageVO> chatMessageVOS =chatService.getTeamChat(userId,teamDetail);
        return ResultUtils.success(chatMessageVOS);
    }

    /**
     * 获取公共聊天信息
     * @param request
     * @return
     */
    @ApiOperation(value = "获取公共聊天信息")
    @GetMapping("/openChat")
    public BaseResponse<List<ChatMessageVO>> getOpenChat(HttpServletRequest request){
        userService.getLoginUser(request);
        List<ChatMessageVO> chatMessageVOS =chatService.getOpenChat();
        return ResultUtils.success(chatMessageVOS);
    }

    /**
     * 获取用户私聊消息列表
     * @param request
     * @return
     */
    @ApiOperation(value = "获取用户私聊消息列表")
    @GetMapping("/message")
    public BaseResponse<List<ChatMessageVO>> getChatMessage(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        List<ChatMessageVO> chatMessageVOS = chatService.getChatMessage(userId);
        return ResultUtils.success(chatMessageVOS);
    }

    /**
     * 删除用户私聊消息
     * @param request
     * @return
     */
    @ApiOperation(value = "删除用户私聊消息")
    @DeleteMapping("/{fromId}")
    public BaseResponse<Boolean> deleteChatMessage(@PathVariable("fromId") Long fromId,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        boolean deleted = chatService.deleteChatMessage(fromId,userId);
        return ResultUtils.success(deleted);
    }

    /**
     * 发送 AI 聊天消息
     * @param messages
     * @param request
     * @return
     */
    @ApiOperation(value = "发送 AI 聊天消息")
    @PostMapping("/aiChat")
    public BaseResponse<GptMessage> toAiChat(@RequestBody List<GptMessage> messages, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        GptMessage message = chatService.toAiChat(userId, messages);
        return ResultUtils.success(message);
    }

    /**
     * 获取 AI 聊天消息
     * @param request
     * @return
     */
    @ApiOperation(value = "获取 AI 聊天消息")
    @GetMapping("/aiChat")
    public BaseResponse<List<GptMessage>> getAiChat(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        List<GptMessage> messages = chatService.getAiChat(userId);
        return ResultUtils.success(messages);
    }
}
