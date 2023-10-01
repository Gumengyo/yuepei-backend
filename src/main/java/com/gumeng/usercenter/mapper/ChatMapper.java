package com.gumeng.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.usercenter.model.domain.Chat;
import com.gumeng.usercenter.model.vo.ChatMessageVO;

import java.util.List;

/**
* @author 顾梦
* @description 针对表【chat(聊天消息表)】的数据库操作Mapper
* @createDate 2023-08-17 15:34:59
* @Entity generator.domain.Chat
*/
public interface ChatMapper extends BaseMapper<Chat> {
    /**
     * 获取私聊信息
     * @param fromId
     * @param toId
     * @return
     */
    List<ChatMessageVO> getPrivateChat(Long fromId, Long toId);

    /**
     * 获取聊天消息
     * @param userId
     * @return
     */
    List<ChatMessageVO> getChatMessage(Long userId);

    /**
     * 获取队伍聊天室消息
     * @param teamId
     * @return
     */
    List<ChatMessageVO> getTeamChat(Long teamId);

    /**
     * 获取公共聊天室消息
     * @return
     */
    List<ChatMessageVO> getOpenChat();
}




