package com.gumeng.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 顾梦
 * @description 队伍添加类
 * @since 2023/7/28
 */
@Data
public class TeamAddRequesat implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 封面图片
     */
    private String coverUrl;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;


}
