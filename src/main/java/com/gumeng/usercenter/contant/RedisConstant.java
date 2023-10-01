package com.gumeng.usercenter.contant;

/**
 * @author 顾梦
 * @description redis 键值
 * @since 2023/8/3
 */
public interface RedisConstant {
    String LOGIN_USER_KEY = "yuepei:login:token:";
    String USER_MATCH = "yuepei:user:match:%s";
    String USER_MATCH_SET = "yuepei:user:match:set";
    String USER_CAPTCHA = "yuepei:user:captcha:%s";

    String USER_JOIN_TEAM = "yupei:user:%s:join_team:%s";
    String DO_CACHE_JOB_LOCK = "yuepei:precachejob:lock";

    String SEND_EMAIL_JOB_LOCK = "yuepei:sendemailjob:lock";

    String BLOG_LIKED_KEY = "yuepei:blog:liked:";
    String BLOG_COMMENT_LIKED_KEY = "yuepei:blog:comment:liked:";

    String MESSAGE_COMMENT_NUM_KEY = "yuepei:message:comment:num:";
    String MESSAGE_LIKE_NUM_KEY = "yuepei:message:like:num:";

    String MESSAGE_LIKE_SET_KEY = "yuepei:message:like:set:";

    String MESSAGE_BLOG_NUM_KEY = "yuepei:message:blog:num:";

    String FEED_KEY = "yuepei:feed:";

    /**
     * 缓存私人聊天
     */
    String CACHE_CHAT_PRIVATE = "yuepei:chat:chat_records:chat_private:";

    /**
     * 缓存ai聊天
     */
    String AI_CHAT = "yuepei:ai:chat:chat_records:";

}
