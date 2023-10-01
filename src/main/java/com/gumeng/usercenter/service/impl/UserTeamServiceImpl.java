package com.gumeng.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.model.domain.Team;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.domain.UserTeam;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.TeamService;
import com.gumeng.usercenter.service.UserService;
import com.gumeng.usercenter.service.UserTeamService;
import com.gumeng.usercenter.mapper.UserTeamMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 顾梦
 * @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
 * @createDate 2023-07-28 21:06:02
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
        implements UserTeamService {

    @Resource
    private UserService userService;

    @Override
    public long countUserTeamByTeamId(long teamId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        return this.count(queryWrapper);
    }




}




