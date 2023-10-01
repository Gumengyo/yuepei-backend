package com.gumeng.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gumeng.usercenter.common.*;
import com.gumeng.usercenter.contant.UserConstant;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.request.UserLoginRequest;
import com.gumeng.usercenter.model.request.UserRegisterRequest;
import com.gumeng.usercenter.model.request.UserUpdateRequest;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author 顾梦
 * @description 用户接口
 * @since 2023/7/27
 */
@Api(tags = "用户信息管理接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest
     * @param request
     * @return
     */
    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest,HttpServletRequest request) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long result = userService.userRegister(userRegisterRequest,request);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取用户信息
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "根据id获取用户信息")
    @GetMapping("/get/{id}")
    public BaseResponse<UserVO> getUserById(@PathVariable Long id,HttpServletRequest request){
        // 校验用户是否登录
        User loginUser = userService.getLoginUser(request);

        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UserVO userVO = userService.getUserById(id, loginUser.getId());
        return ResultUtils.success(userVO);
    }

    /**
     * 用户账号登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @ApiOperation(value = "用户账号登录")
    @PostMapping("/login/account")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户邮箱登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @ApiOperation(value = "用户邮箱登录")
    @PostMapping("/login/email")
    public BaseResponse<User> userEmailLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userEmail = userLoginRequest.getUserEmail();
        String code = userLoginRequest.getCode();
        if (StringUtils.isAnyBlank(userEmail, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userEmailLogin(userEmail, code, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户退出
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "用户退出")
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "获取当前用户")
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH,"未登录");
        }
        User currentUser = (User) userObj;
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 搜索用户
     * @param username
     * @param request
     * @return
     */
    @ApiOperation(value = "搜索用户")
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 分页获取用户列表
     * @param searchText
     * @param pageParams
     * @return
     */
    @ApiOperation(value = "分页获取用户列表")
    @GetMapping("/list")
    public BaseResponse<PageResult<User>> listUser(String searchText,PageParams pageParams) {

        PageResult<User> userPageResult = userService.listUser(searchText,pageParams);

        return ResultUtils.success(userPageResult);
    }

    /**
     * 删除用户
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "删除用户")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     * @param user
     * @param request
     * @return
     */
    @ApiOperation(value = "更新用户")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 1. 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 2.校验权限
        // 3.触发更新
        Boolean result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     *
     * @param tagNameList
     * @return
     */
    @ApiOperation(value = "根据标签搜索用户")
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 获取用户标签
     * @param request
     * @return
     */
    @ApiOperation(value = "获取用户标签")
    @GetMapping("/tags")
    public BaseResponse<List<String> > getUserTags(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);

        List<String> tagList =  userService.getUserTags(loginUser.getId());

        return ResultUtils.success(tagList);
    }

    /**
     * 更新用户标签
     * @param tags
     * @param request
     * @return
     */
    @ApiOperation(value = "更新用户标签")
    @PutMapping("/update/tags")
    public BaseResponse<Boolean> updateUserTags(@RequestBody List<String> tags,HttpServletRequest request){

        if (tags == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签不能为空");
        }

        boolean updateed =  userService.updateUserTags(tags,request);
        if (!updateed){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改标签失败");
        }
        return ResultUtils.success(updateed);
    }

    /**
     * 获取推荐用户
     * @param pageParams
     * @param num
     * @param request
     * @return
     */
    @ApiOperation(value = "获取推荐用户")
    @GetMapping("/match")
    public BaseResponse<PageResult<UserVO>> matchUsers(PageParams pageParams,int num, HttpServletRequest request){
        if (num <= 0 || num > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
         if (loginUser.getTags() == null){
            return ResultUtils.error(ErrorCode.NULL_ERROR);
        }
        List<UserVO> userVOList = userService.matchUsers(num, loginUser);
        PageResult<UserVO> pageResult = new PageResult<>(userVOList,userVOList.size(), pageParams.getPageNum(), pageParams.getPageSize());
        return ResultUtils.success(pageResult);
    }

    /**
     * 发送验证码
     * @param email
     * @return
     */
    @ApiOperation(value = "发送验证码")
    @GetMapping("/sendCode")
    public BaseResponse<Boolean> sentCode(String email){
        boolean sended = userService.sendCode(email);
        return ResultUtils.success(sended);
    }

    /**
     * 校验验证码
     * @param email
     * @param code
     * @return
     */
    @ApiOperation(value = "校验验证码")
    @GetMapping("/checkCode")
    public BaseResponse<Boolean> checkCode(String email,String code){

        if (StringUtils.isAnyBlank(email,code)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        boolean checked = userService.checkCode(email,code);
        return ResultUtils.success(checked);
    }

    /**
     * 忘记密码
     * @param email
     * @return
     */
    @ApiOperation(value = "忘记密码")
    @GetMapping("/forget")
    public BaseResponse<String> forget(String email){
        if (StringUtil.isBlank(email)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.getUserByEmail(email);
        userService.sendCode(email);
        return ResultUtils.success(user.getUsername());
    }

    /**
     * 更新用户密码
     * @param userUpdateRequest
     * @return
     */
    @ApiOperation(value = "更新用户密码")
    @PutMapping("/forget")
    public BaseResponse<Boolean> updateUserPassword(@RequestBody UserUpdateRequest userUpdateRequest){
       if (userUpdateRequest == null){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
       }
        boolean updated = userService.updateUserPassword(userUpdateRequest);
        return ResultUtils.success(updated);
    }


}
