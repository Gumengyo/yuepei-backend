package com.gumeng.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 顾梦
 * @description 用户退出队伍
 * @since 2023/7/28
 */
@Data
public class TeamQuitRequesat implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * id
     */
    private Long teamId;

}
