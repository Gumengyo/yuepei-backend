package com.gumeng.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.mapper.FollowMapper;
import com.gumeng.usercenter.model.domain.Follow;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.FollowService;
import com.gumeng.usercenter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 顾梦
 * @description 针对表【follow】的数据库操作Service实现
 * @createDate 2023-08-13 14:06:58
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private FollowMapper followMapper;

    @Override
    public boolean followUser(Long followUserId, Long userId) {

        if (followUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Follow follow = getFollow(followUserId, userId);

        if (follow != null) {
            return removeById(follow);
        } else {
            follow = new Follow();
            follow.setFollowUserId(followUserId);
            follow.setUserId(userId);
            follow.setCreateTime(LocalDateTime.now());
            return save(follow);
        }
    }

    @Override
    public Follow getFollow(Long followUserId, Long userId) {
        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("followUserId", followUserId)
                .eq("userId", userId);
        return getOne(queryWrapper);
    }

    @Override
    public List<UserVO> getMyFollow(Long userId) {
        return followMapper.getMyFollow(userId);
    }

    @Override
    public List<UserVO> getMyFans(Long userId) {
        return followMapper.getMyFans(userId);
    }

}
