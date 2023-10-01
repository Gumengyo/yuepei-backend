package com.gumeng.usercenter.controller;

import com.gumeng.usercenter.common.BaseResponse;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.common.ResultUtils;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.FollowService;
import com.gumeng.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 顾梦
 * @description 关注管理接口
 * @since 2023/8/13
 */
@RestController
@RequestMapping("/follow")
@Api(tags = "关注管理接口")
@Slf4j
public class FollowController {
    @Resource
    FollowService followService;

    @Resource
    UserService userService;

    /**
     * 关注用户
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "关注用户")
    @PostMapping("/{id}")
    public BaseResponse<Boolean> followUser(@PathVariable("id") Long id, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        boolean save = followService.followUser(id, userId);
        return ResultUtils.success(save);
    }

    /**
     * 获取我关注的用户
     * @param request
     * @return
     */
    @ApiOperation(value = "获取我关注的用户")
    @GetMapping("/my")
    public BaseResponse<List<UserVO>> getMyFollow(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        List<UserVO> userVOList = followService.getMyFollow(userId);
        return ResultUtils.success(userVOList);
    }

    /**
     * 获取我的粉丝
     * @param request
     * @return
     */
    @ApiOperation(value = "获取我的粉丝")
    @GetMapping("/fans")
    public BaseResponse<List<UserVO>> getMyFans(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        List<UserVO> userVOList = followService.getMyFans(userId);
        return ResultUtils.success(userVOList);
    }
}
