package com.gumeng.usercenter.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author 顾梦
 * @description 队伍成员包装类
 * @since 2023/8/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamUserVO extends UserVO {

    /**
     * 加入队伍时间
     */
    private LocalDateTime joinTime;

}
