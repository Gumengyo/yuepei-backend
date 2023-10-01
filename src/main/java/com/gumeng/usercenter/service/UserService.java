package com.gumeng.usercenter.service;

import com.gumeng.usercenter.common.PageParams;
import com.gumeng.usercenter.common.PageResult;
import com.gumeng.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.usercenter.model.request.UserRegisterRequest;
import com.gumeng.usercenter.model.request.UserUpdateRequest;
import com.gumeng.usercenter.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest   用户注册信息
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest,HttpServletRequest request);

    /**
     * 用户账号登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     *
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    // [加入编程导航](https://t.zsxq.com/0emozsIJh) 深耕编程提升【两年半】、国内净值【最高】的编程社群、用心服务【20000+】求学者、帮你自学编程【不走弯路】

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    boolean updateUser(User user, User loginUser);

    /**
     * 判断用户身份
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 判断用户身份
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);


    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 查询推荐用户列表
     *
     * @param pageParams
     * @return
     */
    PageResult<User> listUser(String userSearch,PageParams pageParams);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<UserVO> matchUsers(int num, User loginUser);

    /**
     * 向邮箱发送验证码
     * @param email
     * @return
     */
    boolean sendCode(String email);

    /**
     * 校验验证码
     * @param email
     * @param code
     * @return
     */
    boolean checkCode(String email, String code);

    /**
     * 用户邮箱登录
     * @param userEmail
     * @param code
     * @param request
     * @return
     */
    User userEmailLogin(String userEmail, String code, HttpServletRequest request);

    /**
     * 根据id获取用户信息
     * @param userId
     * @param loginUserId
     * @return
     */
    UserVO getUserById(Long userId, Long loginUserId);

    /**
     * 修改用户标签信息
     * @param tags
     * @param request
     * @return
     */
    boolean updateUserTags(List<String> tags, HttpServletRequest request);

    /**
     * 查询用户标签信息
     * @param id
     * @return
     */
    List<String> getUserTags(Long id);

    /**
     * 根据邮箱号查询用户
     * @param email
     * @return
     */
    User getUserByEmail(String email);

    /**
     * 修改用户密码
     * @param userUpdateRequest
     * @return
     */
    boolean updateUserPassword(UserUpdateRequest userUpdateRequest);
}
