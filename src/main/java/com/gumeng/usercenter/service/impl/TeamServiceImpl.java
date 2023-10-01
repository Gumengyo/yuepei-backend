package com.gumeng.usercenter.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.common.ResultUtils;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.mapper.TeamMapper;
import com.gumeng.usercenter.model.domain.Team;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.domain.UserTeam;
import com.gumeng.usercenter.common.PageParams;
import com.gumeng.usercenter.common.PageResult;
import com.gumeng.usercenter.model.request.TeamQuery;
import com.gumeng.usercenter.model.enums.TeamStatusEnum;
import com.gumeng.usercenter.model.request.TeamJoinRequesat;
import com.gumeng.usercenter.model.request.TeamQuitRequesat;
import com.gumeng.usercenter.model.request.TeamUpdateRequesat;
import com.gumeng.usercenter.model.vo.TeamUserVO;
import com.gumeng.usercenter.model.vo.TeamVO;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.ChatService;
import com.gumeng.usercenter.service.TeamService;
import com.gumeng.usercenter.service.UserService;
import com.gumeng.usercenter.service.UserTeamService;
import com.gumeng.usercenter.utils.QiniuUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.EOFException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.gumeng.usercenter.contant.CommonConstant.QINIU_DOMAIN;
import static com.gumeng.usercenter.contant.RedisConstant.USER_JOIN_TEAM;
import static com.gumeng.usercenter.contant.TeamConstant.TEAM_DEFAULT_COVER;

/**
 * @author 顾梦
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-07-28 21:02:34
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    ChatService chatService;

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        final long userId = loginUser.getId();
        // 3、校验信息
        // 1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        // 2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称过长");
        }
        // 3. 描述 <= 200
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }

        // 4. status 是否公开（int）不传默认为0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        // 5. 如果status 是加密状态，一定要有密码；且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if ((StringUtils.isBlank(password) || password.length() > 32)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码格式不正确");
            }
        }

        // 6. 超时时间 > 当前时间
        LocalDateTime expireTime = team.getExpireTime();
        if (expireTime != null && LocalDateTime.now().isAfter(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超过时间 < 当前时间");
        }
        // 7. 校验用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = teamMapper.selectCount(queryWrapper);
        if (hasTeamNum > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        // 8.插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        int insert = teamMapper.insert(team);
        Long teamId = team.getId();
        if (insert <= 0 || teamId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        // 9.插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(LocalDateTime.now());
        boolean save = userTeamService.save(userTeam);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public PageResult<Team> queryTeamList(PageParams pageParams, TeamQuery teamQuery) {
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = new Page<>(pageParams.getPageNum(), pageParams.getPageSize());
        Page<Team> resultPage = teamMapper.selectPage(teamPage, queryWrapper);

        long total = resultPage.getTotal();

        return new PageResult<>(resultPage.getRecords(), total, pageParams.getPageNum(), pageParams.getPageSize());
    }

    @Override
    public PageResult<TeamVO> listTeams(PageParams pageParams, TeamQuery teamQuery, User loginUser) {
        // 判断用户身份
        boolean isAdmin = userService.isAdmin(loginUser);
        Long pageNum = pageParams.getPageNum();
        Long pageSize = pageParams.getPageSize();
        if (pageSize > 10 || pageSize < 0 || pageNum < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        // 拼装查询条件
        if (teamQuery != null) {
            // 根据 队伍id 查询
            Long id = teamQuery.getId();
            queryWrapper.eq(id != null, Team::getId, id);
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in(Team::getId, idList);
            }
            // 根据搜索关键词词匹配（队伍和队伍简介）
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw ->
                        qw.like(Team::getName, searchText).or()
                                .like(Team::getDescription, searchText));
            } else {
                // 根据队伍名称查询
                String name = teamQuery.getName();
                queryWrapper.like(StringUtils.isNotBlank(name), Team::getName, name);
                // 根据队伍简介查询
                String description = teamQuery.getDescription();
                queryWrapper.like(StringUtils.isNotBlank(description), Team::getDescription, description);
            }

            // 根据人数限制查询
            Integer maxNum = teamQuery.getMaxNum();
            queryWrapper.eq(maxNum != null && maxNum > 0, Team::getMaxNum, maxNum);
            // 根据创建人来查询
            Long userId = teamQuery.getUserId();
            queryWrapper.eq(userId != null && userId > 0, Team::getUserId, userId);

            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq(status != null, Team::getStatus, status);
        }
        // 不展示已过期队伍
        queryWrapper.and(qw -> qw.gt(Team::getExpireTime, LocalDateTime.now())
                .or().isNull(Team::getExpireTime));
        Page<Team> teamPage = new Page<>(pageNum,pageSize);
        Page<Team> pageResult = this.page(teamPage, queryWrapper);
        List<Team> teamLsit = pageResult.getRecords();
        if (CollectionUtils.isEmpty(teamLsit)) {
            return new PageResult<>(new ArrayList<>(),0, pageNum,pageSize);
        }
        List<TeamVO> teamVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamLsit) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }

            // 查询加入队伍的用户数量
            long hasJoinNum = userTeamService.countUserTeamByTeamId(team.getId());
            User user = userService.getById(userId);
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);

            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);

                teamVO.setHasJoinNum(hasJoinNum);
                teamVO.setCreateUser(userVO);
            }
            teamVOList.add(teamVO);
        }

        final List<Long> teamIdList = teamVOList.stream().map(TeamVO::getId).collect(Collectors.toList());
        // 判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        if(loginUser != null) {
            wrapper.eq("userId", loginUser.getId());
            wrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(wrapper);
            // 已加入的队伍 id 集合
            List<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
            teamVOList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        }
        return new PageResult<>(teamVOList,pageResult.getTotal(),pageNum,pageSize);
    }

    @Override
    public boolean updateTeam(TeamUpdateRequesat teamUpdateRequesat, User loginUser) {
        if (teamUpdateRequesat == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequesat.getId();
        Team oldTeam = getTeamById(id);

        if (!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequesat.getStatus());
        if (TeamStatusEnum.SECRET.equals(statusEnum) && StringUtils.isBlank(teamUpdateRequesat.getPassword())) {
            if (StringUtils.isBlank(oldTeam.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequesat, updateTeam);
        updateTeam.setUpdateTime(LocalDateTime.now());
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequesat teamJoinRequesat, User loginUser) {
        // 校验传入参数
        if (teamJoinRequesat == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequesat.getTeamId();
        //判断队伍是否存在
        Team team = getTeamById(teamId);

        // 判断队伍是否过期
        LocalDateTime expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        // 禁止加入私有队伍
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        // 校验加密队伍的密码
        String password = teamJoinRequesat.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        // 校验已加入队伍数
        Long userId = loginUser.getId();
        // 分布式锁
        String key = String.format(USER_JOIN_TEAM,userId,teamId);
        RLock lock = redissonClient.getLock(key);
        try {
            // 拿到锁后执行
            int i = 0; // 重试次数
            while (i < 3) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(queryWrapper);
                    if (hasJoinNum >= 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入 5 个队伍");
                    }
                    // 不能重复加入已加入的队伍
                    queryWrapper.clear();
                    queryWrapper.eq("userId", userId)
                            .eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(queryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    // 校验队伍人数
                    queryWrapper.clear();
                    long teamHasJoinNum = userTeamService.countUserTeamByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    // 修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(LocalDateTime.now());
                    return userTeamService.save(userTeam);
                }
                i++;
            }
        } catch (Exception e) {
            log.error("doCacheRecommendUser error",e);
        }finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequesat teamQuitRequesat, User loginUser) {

        // 1.校验请求参数
        if (teamQuitRequesat == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.校验队伍是否存在
        Long teamId = teamQuitRequesat.getTeamId();
        Team team = getTeamById(teamId);

        // 3. 校验当前用户是否已经加入队伍
        Long userId = loginUser.getId();
        boolean isJoinTeam = isJoinTeam(teamId, userId);
        if (!isJoinTeam) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = userTeamService.countUserTeamByTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        // 4.如果在队伍中
        // 4.1 只剩一个人，队伍解散
        if (teamHasJoinNum == 1) {
            // 删除队伍聊天室记录
            chatService.deleteTeamChatMessage(teamId);
            this.removeById(teamId);
        } else {
            // 4.2 还有其他人
            // 如果是队长退出队伍，权限转移给加入队伍时间最早的用户
            Long teamUserId = team.getUserId();
            if (teamUserId.equals(userId)) {
                // select * from user_team from teamId = ? and userId != ?
                queryWrapper.clear();
                queryWrapper.eq("teamId", teamId)
                        .ne("userId", teamUserId)
                        .orderByAsc("joinTime");
                List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
                if (CollectionUtils.isEmpty(userTeamList)) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam firstUser = userTeamList.get(0);
                Long nextTeamLeaderId = firstUser.getUserId();
                team.setUserId(nextTeamLeaderId);
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍的队长信息失败");
                }
            }
        }
        // 移除关系
        queryWrapper.clear();
        queryWrapper.eq("userId",userId)
                .eq("teamId",teamId);

        return userTeamService.remove(queryWrapper);
    }

    private boolean isJoinTeam(long teamId,long userId){

        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId)
                .eq("userId", userId);
        long count = userTeamService.count(queryWrapper);
        return count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long teamId, User loginUser) {
        // 1.校验请求参数
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.校验队伍是否存在
        Team team = getTeamById(teamId);

        // 3.校验当前用户是不是队伍的队长
        boolean isAdmin = userService.isAdmin(loginUser);
        if (!isAdmin && !team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }

        // 4.移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(queryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍聊天室记录
        chatService.deleteTeamChatMessage(teamId);

        // 5.删除队伍
        return removeById(teamId);
    }

    @Override
    public TeamVO getTeamDetail(Long id, Long userId) {

        Team team = getTeamById(id);
        Long teamUserId = team.getUserId();
        UserVO userVO = userService.getUserById(teamUserId, null);
        TeamVO teamVO = new TeamVO();
        BeanUtils.copyProperties(team, teamVO);
        teamVO.setCreateUser(userVO);
        long hasJoinNum = userTeamService.countUserTeamByTeamId(id);
        teamVO.setHasJoinNum(hasJoinNum);
        boolean hasJoin = isJoinTeam(id, userId);
        teamVO.setHasJoin(hasJoin);
        return teamVO;
    }

    /**
     * 根据 id 获取队伍信息
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    @Override
    public List<TeamUserVO> getTeamMember(Long id) {
        List<UserTeam> list = userTeamService.query().eq("teamId", id)
                .orderByAsc("joinTime").list();
        List<TeamUserVO> teamUserVOList = list.stream().map(userTeam -> {
            Long userId = userTeam.getUserId();
            UserVO userVO = userService.getUserById(userId, null);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(userVO,teamUserVO);
            teamUserVO.setJoinTime(userTeam.getJoinTime());
            return teamUserVO;
        }).collect(Collectors.toList());

        return teamUserVOList;
    }

    @Override
    @Transactional
    public boolean kickTeamMember(Long teamId, Long userId, User loginUser) {

        if (teamId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = this.getById(teamId);
        Long loginUserId = loginUser.getId();

        // 验证身份、只有队长或管理员有权限
        boolean isAdmin = userService.isAdmin(loginUser);
        if (!Objects.equals(loginUserId, team.getUserId()) && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        if (Objects.equals(userId, loginUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队长不能删除自己");
        }

        QueryWrapper<UserTeam> wrapper = new QueryWrapper<>();
        wrapper.eq("teamId", teamId)
                .eq("userId", userId);

        return userTeamService.remove(wrapper);
    }

    @Override
    public String uploadCover(MultipartFile file,Long id, User loginUser) {

        if (file == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = getTeamById(id);
        String coverUrl = team.getCoverUrl();

        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = IdUtil.simpleUUID() + suffix;

        try {
            // 将文件上传到七牛云服务器
            QiniuUtils.upload2Qiniu(file.getBytes(),fileName);
            // 删除原有头像
            if (StringUtils.isNotBlank(coverUrl) && !TEAM_DEFAULT_COVER.equals(coverUrl)){
                String oldFileName = StringUtils.substringAfterLast(coverUrl,'/');
                QiniuUtils.deleteFileFromQiniu(oldFileName);
            }

            coverUrl = QINIU_DOMAIN + fileName;
            team.setCoverUrl(coverUrl);
            boolean updated = updateById(team);
            if (!updated){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改封面失败");
            }

            return coverUrl;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图片上传失败");
        }
    }

}




