package com.gumeng.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.gumeng.usercenter.common.*;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.model.domain.Team;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.domain.UserTeam;
import com.gumeng.usercenter.model.request.*;
import com.gumeng.usercenter.model.vo.TeamUserVO;
import com.gumeng.usercenter.model.vo.TeamVO;
import com.gumeng.usercenter.service.TeamService;
import com.gumeng.usercenter.service.UserService;
import com.gumeng.usercenter.service.UserTeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author 顾梦
 * @description 队伍接口
 * @since 2023/7/27
 */
@Api(tags = "队伍信息管理接口")
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    TeamService teamService;

    @Resource
    UserService userService;

    @Resource
    UserTeamService userTeamService;

    /**
     * 添加队伍
     *
     * @param teamAddRequesat
     * @param request
     * @return
     */
    @ApiOperation(value = "添加队伍")
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequesat teamAddRequesat, HttpServletRequest request) {
        if (teamAddRequesat == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequesat, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 删除队伍
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @ApiOperation(value = "删除队伍")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(deleteRequest.getId(), loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新队伍
     *
     * @param teamUpdateRequesat
     * @param request
     * @return
     */
    @ApiOperation(value = "更新队伍")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequesat teamUpdateRequesat, HttpServletRequest request) {
        if (teamUpdateRequesat == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequesat, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 获取队伍详情
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "获取队伍详情")
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    /**
     * 获取队伍列表
     *
     * @param pageParams
     * @param teamQuery
     * @param request
     * @return
     */
    @ApiOperation(value = "获取队伍列表")
    @GetMapping("/list")
    public BaseResponse<PageResult<TeamVO>> listTeams(PageParams pageParams, TeamQuery teamQuery, HttpServletRequest request) {
        User loginUser = null;
        try{
            loginUser = userService.getLoginUser(request);
        }catch (Exception ignored){}

        PageResult<TeamVO> pageResult = teamService.listTeams(pageParams, teamQuery, loginUser);

        return ResultUtils.success(pageResult);
    }

    /**
     * 分页获取队伍列表
     * @param pageParams
     * @param teamQuery
     * @return
     */
    @ApiOperation(value = "分页获取队伍列表")
    @GetMapping("/list/page")
    public BaseResponse<PageResult<Team>> listPageTeams(PageParams pageParams, TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PageResult<Team> teamPageResult = teamService.queryTeamList(pageParams, teamQuery);

        return ResultUtils.success(teamPageResult);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequesat
     * @param request
     * @return
     */
    @ApiOperation(value = "加入队伍")
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequesat teamJoinRequesat, HttpServletRequest request) {
        if (teamJoinRequesat == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequesat, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequesat
     * @param request
     * @return
     */
    @ApiOperation(value = "退出队伍")
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequesat teamQuitRequesat, HttpServletRequest request) {
        if (teamQuitRequesat == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequesat, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取我创建的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @ApiOperation(value = "获取我创建的队伍")
    @GetMapping("/list/my/create")
    public BaseResponse<PageResult<TeamVO>> listMyTeams(PageParams pageParams, TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        loginUser.setUserRole(1);
        PageResult<TeamVO> pageResult = teamService.listTeams(pageParams,teamQuery, loginUser);
        return ResultUtils.success(pageResult);
    }

    /**
     * 获取我加入的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @ApiOperation(value = "获取我加入的队伍")
    @GetMapping("/list/my/join")
    public BaseResponse<PageResult<TeamVO>> listMyJoinTeams(PageParams pageParams, TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        loginUser.setUserRole(1);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 取出不重复的队伍id
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        if (CollectionUtils.isEmpty(idList)){
            return ResultUtils.success(null);
        }
        teamQuery.setIdList(idList);
        PageResult<TeamVO> pageResult = teamService.listTeams(pageParams,teamQuery, loginUser);
        return ResultUtils.success(pageResult);
    }

    /**
     * 获取队伍详情
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "获取队伍详情")
    @GetMapping("/{id}")
    public BaseResponse<TeamVO> getTeamDetail(@PathVariable("id") Long id, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        TeamVO teamVO = teamService.getTeamDetail(id,userId);
        return ResultUtils.success(teamVO);
    }

    /**
     * 获取队伍成员
     * @param id
     * @return
     */
    @ApiOperation(value = "获取队伍成员")
    @GetMapping("/member/{id}")
    public BaseResponse<List<TeamUserVO>> getTeamMember(@PathVariable("id") Long id){
        List<TeamUserVO> teamUserVOList = teamService.getTeamMember(id);
        return ResultUtils.success(teamUserVOList);
    }

    /**
     * 踢出队伍成员
     * @param teamKickUserRequest
     * @param request
     * @return
     */
    @ApiOperation(value = "踢出队伍成员")
    @PostMapping("/kick")
    public BaseResponse<Boolean> kickTeamMember(@RequestBody TeamKickUserRequest teamKickUserRequest,
                                                HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if (teamKickUserRequest == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Long teamId = teamKickUserRequest.getTeamId();
        Long userId = teamKickUserRequest.getUserId();
        boolean kicked = teamService.kickTeamMember(teamId, userId, loginUser);
        return ResultUtils.success(kicked);
    }

    /**
     * 上传队伍封面
     * @param file
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "上传队伍封面")
    @PutMapping("/cover")
    public BaseResponse<String> uploadCover(MultipartFile file,Long id,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);

        if (file == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String coverUrl = teamService.uploadCover(file, id, loginUser);
        return ResultUtils.success(coverUrl);
    }
}
