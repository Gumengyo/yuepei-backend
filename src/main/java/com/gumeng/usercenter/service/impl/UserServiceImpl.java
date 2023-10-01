package com.gumeng.usercenter.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.common.PageParams;
import com.gumeng.usercenter.common.PageResult;
import com.gumeng.usercenter.contant.UserConstant;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.mapper.UserMapper;
import com.gumeng.usercenter.model.domain.Follow;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.request.UserRegisterRequest;
import com.gumeng.usercenter.model.request.UserUpdateRequest;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.FollowService;
import com.gumeng.usercenter.service.UserService;
import com.gumeng.usercenter.utils.AlgorithmUtils;
import com.gumeng.usercenter.utils.EmailService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gumeng.usercenter.contant.EmailConstant.EMAIL_TEMPLATE;
import static com.gumeng.usercenter.contant.EmailConstant.EMATL_SUBJECT;
import static com.gumeng.usercenter.contant.RedisConstant.*;
import static com.gumeng.usercenter.contant.UserConstant.*;

/**
 * 用户服务实现类
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private EmailService emailService;

    @Resource
    private RedissonClient redissonClient;


    @Resource
    private FollowService followService;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "gumeng";

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册信息
     * @return 新用户 id
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest,HttpServletRequest request) {

        String email = userRegisterRequest.getEmail();
        String code = userRegisterRequest.getCode();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String key = String.format(USER_CAPTCHA,email);
        String captcha = (String) redisTemplate.opsForValue().get(key);

        String planetCode = StringUtils.substring(IdUtil.simpleUUID(), 0, 10);
        // 1. 校验

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,email,code)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }
        if (StringUtils.isBlank(captcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码已过期");
        }
        if (!code.equals(captcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }
        redisTemplate.delete(key);
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 邮箱不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱重复");
        }
        // 悦配号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            planetCode = StringUtils.substring(IdUtil.simpleUUID(), 0, 10);
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        if(email.contains("@qq.com")) {
            user.setAvatarUrl(String.format(USER_QQ_AVATAR,email));
        }else {
            user.setAvatarUrl(USER_DEFAULT_AVATAR);
        }
        user.setEmail(email);
        user.setUsername("user_" + StringUtils.substring(userAccount,0,6));
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return user.getId();
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户邮箱登录
     * @param userEmail
     * @param code
     * @param request
     * @return
     */
    @Override
    public User userEmailLogin(String userEmail, String code, HttpServletRequest request) {

        // 校验参数
        if (StringUtils.isAnyBlank(userEmail, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (code.length() != 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请输入6位验证码");
        }

        // 校验验证码
        String key = String.format(USER_CAPTCHA,userEmail);
        String captcha = (String) redisTemplate.opsForValue().get(key);

        if (captcha == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码已过期");
        }

        if (!captcha.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }

        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email",userEmail);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            redisTemplate.delete(key);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该邮箱未注册用户");
        }

        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        redisTemplate.delete(key);
        return safetyUser;
    }

    /**
     * 根据 id 获取用户
     * @param userId
     * @param loginUserId
     * @return
     */
    @Override
    public UserVO getUserById(Long userId, Long loginUserId) {
        User user = this.getById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        if (loginUserId != null ){
            Long count = followService.query().eq("followUserId", userId)
                    .eq("userId", loginUserId).count();
            userVO.setIsFollow(count > 0);
        }

        return userVO;
    }

    /**
     * 修改用户标签
     * @param tags
     * @param request
     * @return
     */
    @Override
    public boolean updateUserTags(List<String> tags, HttpServletRequest request) {
        // 校验参数
        if (tags == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签不能为空");
        }
        User loginUser = getLoginUser(request);
        Gson gson = new Gson();

        String tagsJson = gson.toJson(tags);
        if (tagsJson.equals(loginUser.getTags())){
            return true;
        }

        User user = new User();
        user.setId(loginUser.getId());
        user.setTags(tagsJson);
        user.setUpdateTime(LocalDateTime.now());
        boolean updated = updateById(user);
        request.removeAttribute(USER_LOGIN_STATE);
        loginUser.setTags(tagsJson);
        request.getSession().setAttribute(USER_LOGIN_STATE, loginUser);

        return updated;
    }

    /**
     * 获取用户标签
     * @param id
     * @return
     */
    @Override
    public List<String> getUserTags(Long id) {

        User user = this.getById(id);
        String tags = user.getTags();

        Gson gson = new Gson();
        return gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
    }

    /**
     * 根据邮箱获取用户
     * @param email
     * @return
     */
    @Override
    public User getUserByEmail(String email) {

        // 校验参数
        if (StringUtil.isBlank(email)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = this.getOne(queryWrapper);
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该邮箱号未注册用户");
        }
        return user;
    }

    @Override
    @Transactional
    public boolean updateUserPassword(UserUpdateRequest userUpdateRequest) {

        String email = userUpdateRequest.getEmail();
        String code = userUpdateRequest.getCode();
        String userPassword = userUpdateRequest.getUserPassword();
        String checkPassword = userUpdateRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(email,code,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }

        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }

        boolean checked = checkCode(email, code);
        if (!checked){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email",email);
        User user = this.getOne(queryWrapper);
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        Long userId = user.getId();

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        user = new User();
        user.setId(userId);
        user.setUserPassword(encryptPassword);
        user.setUpdateTime(LocalDateTime.now());
        redisTemplate.delete(String.format(USER_CAPTCHA,email));

        return this.updateById(user);
    }


    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setProfile(originUser.getProfile());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public boolean updateUser(User user, User loginUser) {
        // 修改用户id
        Long userId = user.getId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 仅管理员和自己可修改
        if (!isAdmin(loginUser) && !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        String username = user.getUsername();
        String profile = user.getProfile();
        Integer gender = user.getGender();
        String phone = user.getPhone();
        String email = user.getEmail();
        if (StringUtils.isAllBlank(username,profile,phone,email) && gender == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (!isAdmin(loginUser)) {
            user.setUserRole(null);
        }
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return loginUser;
    }

    /**
     * 获取用户列表
     * @param searchText
     * @param pageParams
     * @return
     */
    @Override
    @SuppressWarnings("all")
    public PageResult<User> listUser(String searchText,PageParams pageParams) {

        Long pageNum = pageParams.getPageNum();
        Long pageSize = pageParams.getPageSize();

        if (pageSize > 10 || pageSize < 0 || pageNum < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.eq("userName",searchText)
                    .or().eq("planetCode",searchText);
        }
        // 创建page分页参数对象，参数：当前页码，每页记录数
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> pageResult = userMapper.selectPage(page, queryWrapper);

        List<User> userList = pageResult.getRecords();

        // 脱敏
        List<User> list = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());

        // 总记录数
        long total = pageResult.getTotal();
        PageResult<User> userPageResult = new PageResult<>(list, total, pageNum, pageSize);

        return userPageResult;
    }

    /**
     * 获取匹配用户列表
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    @SuppressWarnings("all")
    public List<UserVO> matchUsers(int num, User loginUser) {
        Long userId = loginUser.getId();
        // 用户列表的下标 => 相似度
        String redisKey = String.format(USER_MATCH, userId);
        // 判断是否存在缓存
        List<UserVO> userVOS = (List<UserVO>) redisTemplate.opsForValue().get(redisKey);
        if (userVOS != null) {
            return userVOS;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        queryWrapper.ne("tags", "[]");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());

        RScoredSortedSet<User> scoredSortedSet = redissonClient.getScoredSortedSet(USER_MATCH_SET);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), userId)) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            double score = AlgorithmUtils.calculateSimilarity(tagList, userTagList);
            scoredSortedSet.add(score, user);
        }
        Collection<ScoredEntry<User>> scoredEntries = scoredSortedSet.entryRangeReversed(0, num - 1);
        List<User> maxDistanceUserList = scoredEntries.stream().map(ScoredEntry::getValue).collect(Collectors.toList());
        redisTemplate.delete(USER_MATCH_SET);
        List<UserVO> userVOList = maxDistanceUserList.stream().map(u -> {
            User user = this.getById(u);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());

        // 缓存到redis，过期时间为6小时
        redisTemplate.opsForValue().set(redisKey, userVOList, 6, TimeUnit.HOURS);

        return userVOList;
    }

    /**
     * 发送验证码
     * @param email
     * @return
     */
    @Override
    public boolean sendCode(String email) {
        String numbers = RandomUtil.randomNumbers(6);
        String key = String.format(USER_CAPTCHA, email);
        // 如果还没过期不发送邮件
        String code = (String) redisTemplate.opsForValue().get(key);
        if (code != null){
            log.info("验证码还未过期，验证码：{}",code);
            return true;
        }
        try {
            // 邮件验证码发送功能，上线时开启
            emailService.sendHtmlMail(email, EMATL_SUBJECT, String.format(EMAIL_TEMPLATE, numbers));
            log.info("发送邮件成功，验证码：{}",numbers);
            // 将验证码缓存到redis
            redisTemplate.opsForValue().set(key, numbers, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("发送邮件失败:{}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 校验验证码
     * @param email
     * @param code
     * @return
     */
    @Override
    public boolean checkCode(String email, String code) {

        if (StringUtils.isAnyBlank(email,code)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        String key = String.format(USER_CAPTCHA, email);
        String captcha = (String) redisTemplate.opsForValue().get(key);

        if (StringUtils.isBlank(captcha)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码已过期");
        }

        if (!captcha.equals(code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }

        return true;
    }


    /**
     * 根据标签搜索用户（SQL 查询版）
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

}

