package com.gumeng.usercenter.model.request;

import lombok.Data;

/**
 * @author 顾梦
 * @description 踢出队伍成员
 * @since 2023/8/15
 */
@Data
public class TeamKickUserRequest {
    private Long teamId;
    private Long userId;
}
