package com.gumeng.usercenter.component;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gumeng.usercenter.model.request.MessageRequest;
import com.gumeng.usercenter.model.vo.ChatMessageVO;
import com.gumeng.usercenter.model.vo.TeamVO;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.ChatService;
import com.gumeng.usercenter.service.TeamService;
import com.gumeng.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.gumeng.usercenter.contant.ChatConstant.*;

/**
 * @author 顾梦
 * @description websocket服务
 * @since 2023/8/16
 */
@ServerEndpoint(value = "/chat/{userId}/{teamId}")
@Component
@Slf4j
public class WebSocketServer {

    /**
     * 保存队伍的连接信息
     */
    private static final Map<String, ConcurrentHashMap<String, Session>> ROOMS = new HashMap<>();

    /**
     * 记录当前在线连接数
     */
    public static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 用户服务
     */
    private static UserService userService;

    /**
     * 聊天服务
     */
    private static ChatService chatService;

    /**
     * 队伍服务
     */
    private static TeamService teamService;

    @Resource
    public void setHeatMapService(UserService userService) {
        WebSocketServer.userService = userService;
    }

    /**
     * 集热地图服务
     *
     * @param teamService 团队服务
     */
    @Resource
    public void setHeatMapService(TeamService teamService) {
        WebSocketServer.teamService = teamService;
    }

    /**
     * 集热地图服务
     *
     * @param chatService 聊天服务
     */
    @Resource
    public void setHeatMapService(ChatService chatService) {
        WebSocketServer.chatService = chatService;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam(value = "teamId") String teamId) {

        if (StringUtils.isBlank(userId) || "undefined".equals(userId)) {
            sendError(sessionMap.get(userId), "参数错误");
            return;
        }

        if (!"NaN".equals(teamId)) {
            if (!"0".equals(teamId)) {
                TeamVO teamDetail = teamService.getTeamDetail(Long.parseLong(teamId), Long.parseLong(userId));
                if (!teamDetail.isHasJoin()) {
                    sendError(session, "未加入队伍");
                    return;
                }
            }
            if (!ROOMS.containsKey(teamId)) {
                ConcurrentHashMap<String, Session> room = new ConcurrentHashMap<>(0);
                room.put(userId, session);
                ROOMS.put(teamId, room);
            } else {
                if (!ROOMS.get(teamId).containsKey(userId)) {
                    ROOMS.get(teamId).put(userId, session);
                }
            }
            log.info("有新用户加入聊天室teamId={}，userId={}, 当前在线人数为：{}", teamId, userId, ROOMS.get(teamId).size());
            ConcurrentHashMap<String, Session> teamSessionMap = ROOMS.get(teamId);
            sendOnlineUsers(teamSessionMap);
        } else {
            sessionMap.put(userId, session);
            log.info("有新用户加入，userId={}, 当前在线人数为：{}", userId, sessionMap.size());
            sendOnlineUsers(sessionMap);
        }

    }

    /**
     * 后台发送在线人数给所有客户端
     */
    private void sendOnlineUsers(Map<String, Session> connectMap) {
        if (connectMap == null){
            return;
        }
        JSONObject result = new JSONObject();
        result.set("users", connectMap.size());
        broadcast(JSONUtil.toJsonStr(result), connectMap);
    }


    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId, @PathParam("teamId") String teamId) {
        if (!"NaN".equals(teamId)) {
            ConcurrentHashMap<String, Session> concurrentHashMap = ROOMS.get(teamId);
            if (concurrentHashMap != null) {
                concurrentHashMap.remove(userId);
                log.info("有一连接关闭，移除聊天室teamId={}中userId={}的用户session, 当前在线人数为：{}", teamId, userId, ROOMS.get(teamId).size());
                sendOnlineUsers(concurrentHashMap);
            }
        } else {
            sessionMap.remove(userId);
            log.info("有一连接关闭，移除userId={}的用户session, 当前在线人数为：{}", userId, sessionMap.size());
        }
    }


    /**
     * 发送失败信息
     *
     * @param session
     * @param errorMessage
     */
    private void sendError(Session session, String errorMessage) {
        JSONObject obj = new JSONObject();
        obj.set("error", errorMessage);
        sendMessage(obj.toString(), session);
    }

    /**
     * 收到客户端消息后调用的方法
     * 后台收到客户端发送过来的消息
     * onMessage 是一个消息的中转站
     * 接受 浏览器端 socket.send 发送过来的 json数据
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId, @PathParam("teamId") String teamId) {
        if (!"NaN".equals(teamId)) {
            log.info("服务端收到聊天室teamId={}中用户userId={}的消息:{}", teamId, userId, message);
        } else {
            log.info("服务端收到用户userId={}的消息:{}", userId, message);
        }
        if ("PING".equals(message)) {
            sendOneMessage(userId, "pong");
            return;
        }
        MessageRequest messageRequest = JSONUtil.toBean(message, MessageRequest.class);
        Long toId = messageRequest.getToId();
        String text = messageRequest.getText();
        UserVO fromUser = userService.getUserById(Long.parseLong(userId), null);
        Integer chatType = messageRequest.getChatType();
        if (chatType == PRIVATE_CHAT) {
            // 私聊
            privateChat(fromUser, toId, text);
        } else if (chatType == TEAM_CHAT) {
            // 队伍聊天室
            teamChat(fromUser, text, Long.parseLong(teamId));
        } else {
            // 公共聊天室
            openChat(fromUser, text);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 服务端发送消息给客户端
     */
    private void sendMessage(String message, Session toSession) {
        try {
            log.info("服务端给客户端[{}]发送消息{}", toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }

    /**
     * 发送一个消息
     *
     * @param userId  用户编号
     * @param message 消息
     */
    public void sendOneMessage(String userId, String message) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 私聊
     *
     * @param fromUser 来自用户
     * @param toId     发送用户id
     * @param text     消息
     */
    private void privateChat(UserVO fromUser, Long toId, String text) {

        Session toSession = sessionMap.get(toId.toString());
        if (toSession != null) {
            ChatMessageVO chatMessageVO = chatService.chatResult(fromUser, text);
            String toJson = JSONUtil.toJsonStr(chatMessageVO);
            sendOneMessage(toId.toString(), toJson);
            log.info("发送给用户userId={}，消息{}", toId, toJson);
            chatService.saveChat(fromUser.getId(), toId, text, null, PRIVATE_CHAT,true);
        }else {
            chatService.saveChat(fromUser.getId(), toId, text, null, PRIVATE_CHAT,false);
        }
    }

    /**
     * 队伍聊天
     *
     * @param fromUser 发送消息用户
     * @param text     聊天内容
     * @param teamId   队伍id
     */
    private void teamChat(UserVO fromUser, String text, Long teamId) {
        ConcurrentHashMap<String, Session> teamSessionMap = ROOMS.get(teamId.toString());
        Long fromId = fromUser.getId();
        TeamVO teamDetail = teamService.getTeamDetail(teamId, fromId);
        Integer maxNum = teamDetail.getMaxNum();
        boolean hasJoin = teamDetail.isHasJoin();
        if (teamSessionMap.size() >= maxNum || !hasJoin) {
            sendError(teamSessionMap.get(fromId.toString()), "未加入队伍或人数已满");
            return;
        }
        if (teamSessionMap.size() > 0) {
            ChatMessageVO chatMessageVO = chatService.chatResult(fromUser, text);
            String toJson = JSONUtil.toJsonStr(chatMessageVO);
            broadcast( toJson, teamSessionMap);
            log.info("用户userId={},发送队伍teamId={},群聊消息={}", fromId, teamId, toJson);
        }
        chatService.saveChat(fromUser.getId(), null, text, teamId, TEAM_CHAT,false);
    }

    private void openChat(UserVO fromUser,String text){
        ConcurrentHashMap<String, Session> teamSessionMap = ROOMS.get("0");
        Long fromId = fromUser.getId();
        if (teamSessionMap.size() > 0) {
            ChatMessageVO chatMessageVO = chatService.chatResult(fromUser, text);
            String toJson = JSONUtil.toJsonStr(chatMessageVO);
            broadcast( toJson, teamSessionMap);
            log.info("用户userId={},发送公共聊天室,群聊消息={}", fromId, toJson);
        }
        chatService.saveChat(fromUser.getId(), null, text, null, OPEN_CHAT,false);
    }

    /**
     * 服务端发送消息给所有客户端
     */
    private void broadcast(String message, Map<String, Session> teamSessionMap) {
        try {
            if (teamSessionMap == null) {
                return;
            }
            for (Session session : teamSessionMap.values()) {
                log.info("服务端给客户端[{}]发送消息{}", session.getId(), message);
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }


}
