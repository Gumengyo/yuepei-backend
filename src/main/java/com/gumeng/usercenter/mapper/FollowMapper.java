package com.gumeng.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.usercenter.model.domain.Follow;
import com.gumeng.usercenter.model.vo.UserVO;

import java.util.List;

/**
* @author 顾梦
* @description 针对表【follow】的数据库操作Mapper
* @createDate 2023-08-13 14:06:58
* @Entity generator.domain.Follow
*/
public interface FollowMapper extends BaseMapper<Follow> {
    /**
     * 获取我关注的
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




