package com.gumeng.usercenter.service;

import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.dto.Message;

import java.util.List;

/**
 * @author 顾梦
 * @description 消息获取
 * @since 2023/8/12
 */
public interface MessageService {


    /**
     * 获取我收到的点赞消息
     * @param loginUser
     * @return
     */
    List<Message> getMyLikeMessage(User loginUser);

    /**
     * 获取收到的未读评论数
     * @param userId
     * @return
     */
    Long getCommentNum(Long userId);

    /**
     * 获取收到的未读点赞数
     * @param userId
     * @return
     */
    Long getLikeNum(Long userId);

    /**
     * 获取未读聊天消息数
     * @param userId
     * @return
     */
    Long getChatNum(Long userId);

    /**
     * 获取关注用户新发布的文章数
     * @param userId
     * @return
     */
    Long getBlogOfFollowNum(Long userId);
}
