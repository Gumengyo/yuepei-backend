package com.gumeng.usercenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.usercenter.model.domain.Follow;
import com.gumeng.usercenter.model.vo.UserVO;

import java.util.List;

/**
* @author 顾梦
* @description 针对表【follow】的数据库操作Service
* @createDate 2023-08-13 14:06:58
*/
public interface FollowService extends IService<Follow> {

    /**
     * 关注用户
     * @param followUserId
     * @param userId
     * @return
     */
    boolean followUser(Long followUserId, Long userId);

    /**
     * 获取关注信息
     * @param followUserId
     * @param userId
     * @return
     */
    Follow getFollow(Long followUserId, Long userId);

    /**
     * 获取我关注的用户
     * @param userId
     * @return
     */
    List<UserVO> getMyFollow(Long userId);

    /**
     * 获取关注我的
     * @param userId
     * @return
     */
    List<UserVO> getMyFans(Long userId);

}
