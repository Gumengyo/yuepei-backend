package com.gumeng.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 顾梦
 * @description 队伍和用户信息封装类（脱敏）
 * @since 2023/7/30
 */
@Data
public class TeamVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名
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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人用户信息
     */
    private UserVO createUser;

    /**
     * 是否已加入队伍
     */
    private boolean hasJoin = false;

    /**
     * 队伍加入人数
     */
    private Long hasJoinNum;

    private static final long serialVersionUID = 1L;

}
