package com.gumeng.usercenter.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.mapper.ChatMapper;
import com.gumeng.usercenter.model.domain.Chat;
import com.gumeng.usercenter.model.dto.GptMessage;
import com.gumeng.usercenter.model.dto.RequestBody;
import com.gumeng.usercenter.model.dto.ResponseBody;
import com.gumeng.usercenter.model.vo.ChatMessageVO;
import com.gumeng.usercenter.model.vo.ChatUserVO;
import com.gumeng.usercenter.model.vo.TeamVO;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.ChatService;
import com.gumeng.usercenter.service.TeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.gumeng.usercenter.contant.ChatConstant.*;
import static com.gumeng.usercenter.contant.RedisConstant.AI_CHAT;
import static com.gumeng.usercenter.contant.RedisConstant.CACHE_CHAT_PRIVATE;

/**
 * @author 顾梦
 * @description 针对表【chat(聊天消息表)】的数据库操作Service实现
 * @createDate 2023-08-17 15:34:59
 */
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
        implements ChatService {

    @Resource
    private ChatMapper chatMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveChat(Long id, Long toId, String text, Long teamId, Integer chatType,boolean isOnline) {
        Chat chat = new Chat();
        chat.setFromId(id);
        chat.setToId(toId);
        chat.setText(text);
        chat.setChatType(chatType);
        chat.setTeamId(teamId);
        chat.setCreateTime(LocalDateTime.now());
        boolean saved = save(chat);
        if (toId != null && saved && !isOnline) {
            String key = CACHE_CHAT_PRIVATE + toId;
            incrementMessageCount(key, id.toString());
        }
    }

    public void incrementMessageCount(String key, String toId) {
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
        Boolean hasKey = hashOperations.hasKey(key, toId);
        if (Boolean.TRUE.equals(hasKey)) {
            hashOperations.increment(key, toId, 1L);
        } else {
            hashOperations.put(key, toId, "1");
        }
    }

    public long getMessageCount(String key, String toId) {
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
        String count = hashOperations.get(key, toId);
        return count != null ? Long.parseLong(count) : 0L;
    }

    public void deleteMessageCount(String key, String toId) {
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
        hashOperations.delete(key, toId);
    }

    public ChatMessageVO chatResult(UserVO fromUser, String text) {
        ChatMessageVO chatMessageVO = new ChatMessageVO();
        ChatUserVO chatUserVO = new ChatUserVO();
        BeanUtils.copyProperties(fromUser, chatUserVO);
        chatMessageVO.setFromUser(chatUserVO);
        chatMessageVO.setText(text);
        chatMessageVO.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        return chatMessageVO;
    }

    @Override
    public List<ChatMessageVO> getPrivateChat(Long fromId, Long toId) {

        if (fromId == null || toId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<ChatMessageVO> privateChat = chatMapper.getPrivateChat(fromId, toId);
        String key = CACHE_CHAT_PRIVATE + fromId;
        deleteMessageCount(key, toId.toString());
        return privateChat;
    }

    @Override
    public List<ChatMessageVO> getChatMessage(Long userId) {
        List<ChatMessageVO> chatMessages = chatMapper.getChatMessage(userId);
        String key = CACHE_CHAT_PRIVATE + userId;
        for (ChatMessageVO chatMessageVO : chatMessages) {
            Long fromId = chatMessageVO.getFromId();
            long count = getMessageCount(key, fromId.toString());
            chatMessageVO.setMessageNum(count);
        }
        return chatMessages;
    }

    @Override
    public List<ChatMessageVO> getTeamChat(Long userId, TeamVO teamDetail) {

        if (!teamDetail.isHasJoin()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "未加入队伍");
        }

        return chatMapper.getTeamChat(teamDetail.getId());

    }

    @Override
    public List<ChatMessageVO> getOpenChat() {

        return chatMapper.getOpenChat();
    }

    @Override
    public boolean deleteChatMessage(Long fromId, Long toId) {

        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fromId", fromId)
                .eq("toId", toId).eq("chatType", PRIVATE_CHAT);
        return remove(queryWrapper);
    }

    @Override
    public GptMessage toAiChat(Long userId, List<GptMessage> messages) {

        if(messages.isEmpty()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"消息不能为空");
        }

        RequestBody requestBody = new RequestBody();
        requestBody.setModel("gpt-3.5-turbo");
        requestBody.setMessages(messages);

        String toJson = JSONUtil.toJsonStr(requestBody);

        String resJSOn = HttpRequest.post(URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .body(toJson)
                .execute()
                .body();
        if (StringUtils.isBlank(resJSOn)){
            return null;
        }
        try {
            Gson gson = new Gson();
            ResponseBody body = gson.fromJson(resJSOn,ResponseBody.class);
            GptMessage message = body.getChoices().get(0).getMessage();
            message.setRole("system");
            messages.add(message);
            String key = AI_CHAT + userId;
            String json = gson.toJson(messages);
            // 保存用户ai对话消息，有效时间5分钟
            stringRedisTemplate.opsForValue().set(key,json,5,TimeUnit.MINUTES);
            return message;
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

    }

    @Override
    public List<GptMessage> getAiChat(Long userId) {
        String key = AI_CHAT + userId;
        Boolean hasKey = stringRedisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(hasKey)){
            Gson gson = new Gson();
            String json = stringRedisTemplate.opsForValue().get(key);
            try {
                // 将 JSON 字符串转换为 List<GptMessage> 对象
                return gson.fromJson(json, new TypeToken<List<GptMessage>>(){}.getType());
            } catch (Exception e) {
                // 处理异常情况
                e.printStackTrace();
            }
        }

        return Collections.emptyList();
    }

    @Override
    @Transactional
    public boolean deleteTeamChatMessage(Long teamId) {

        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("chatType",2)
                .eq("teamId",teamId);

        return remove(queryWrapper);
    }
}




