package com.gumeng.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.usercenter.model.domain.Chat;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.dto.Choices;
import com.gumeng.usercenter.model.dto.GptMessage;
import com.gumeng.usercenter.model.vo.ChatMessageVO;
import com.gumeng.usercenter.model.vo.TeamVO;
import com.gumeng.usercenter.model.vo.UserVO;

import java.util.List;

/**
* @author 顾梦
* @description 针对表【chat(聊天消息表)】的数据库操作Service
* @createDate 2023-08-17 15:34:59
*/
public interface ChatService extends IService<Chat> {

    /**
     * 保持聊天记录
     * @param id
     * @param toId
     * @param text
     * @param teamId
     * @param chatType
     * @param isOnline
     */
    void saveChat(Long id, Long toId, String text, Long teamId, Integer chatType,boolean isOnline);

    ChatMessageVO chatResult(UserVO fromUser, String text);

    /**
     * 获取私聊信息
     * @param fromId
     * @param toId
     * @return
     */
    List<ChatMessageVO> getPrivateChat(Long fromId,Long toId);

    /**
     * 获取聊天信息
     * @param userId
     * @return
     */
    List<ChatMessageVO> getChatMessage(Long userId);

    /**
     * 获取队伍聊天室消息
     * @param userId
     * @param teamDetail
     * @return
     */
    List<ChatMessageVO> getTeamChat(Long userId, TeamVO teamDetail);

    /**
     * 获取公共聊天室消息
     * @return
     */
    List<ChatMessageVO> getOpenChat();

    /**
     * 删除聊天记录
     * @param fromId
     * @param toId
     * @return
     */
    boolean deleteChatMessage(Long fromId, Long toId);

    /**
     * Ai 助手对话
     * @param userId
     * @param messages
     * @return
     */
    GptMessage toAiChat(Long userId, List<GptMessage> messages);

    /**
     * 获取 Ai 助手聊天记录
     * @param userId
     * @return
     */
    List<GptMessage> getAiChat(Long userId);

    /**
     * 删除队伍聊天室消息记录
     * @param teamId
     * @return
     */
    boolean deleteTeamChatMessage(Long teamId);
}
