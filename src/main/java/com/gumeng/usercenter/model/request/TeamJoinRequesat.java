package com.gumeng.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 顾梦
 * @description 用户加入队伍
 * @since 2023/7/28
 */
@Data
public class TeamJoinRequesat implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;


}
