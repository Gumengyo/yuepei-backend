package com.gumeng.usercenter.service;

import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.domain.UserTeam;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.usercenter.model.vo.UserVO;

import java.util.List;

/**
 * @author 顾梦
 * @description 针对表【user_team(用户队伍关系)】的数据库操作Service
 * @createDate 2023-07-28 21:06:02
 */
public interface UserTeamService extends IService<UserTeam> {
    /**
     * 查询当前队伍人数
     * @param teamId
     * @return
     */
    long countUserTeamByTeamId(long teamId);

}