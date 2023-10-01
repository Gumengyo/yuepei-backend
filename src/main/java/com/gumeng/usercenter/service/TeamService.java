package com.gumeng.usercenter.service;

import com.gumeng.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.common.PageParams;
import com.gumeng.usercenter.common.PageResult;
import com.gumeng.usercenter.model.request.TeamQuery;
import com.gumeng.usercenter.model.request.TeamJoinRequesat;
import com.gumeng.usercenter.model.request.TeamQuitRequesat;
import com.gumeng.usercenter.model.request.TeamUpdateRequesat;
import com.gumeng.usercenter.model.vo.TeamUserVO;
import com.gumeng.usercenter.model.vo.TeamVO;
import com.gumeng.usercenter.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author 顾梦
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2023-07-28 21:02:34
 */
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 分页查询队伍信息
     *
     * @param pageParams 分页参数
     * @param teamQuery  查询参数
     */
    PageResult<Team> queryTeamList(PageParams pageParams, TeamQuery teamQuery);

    /**
     * 搜索队伍
     *
     * @param pageParams
     * @param teamQuery
     * @param loginUser
     * @return
     */
    PageResult<TeamVO> listTeams(PageParams pageParams, TeamQuery teamQuery, User loginUser);

    /**
     * 更新队伍
     *
     * @param teamUpdateRequesat
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequesat teamUpdateRequesat,User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinRequesat
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequesat teamJoinRequesat, User loginUser);

    /**
     * 退出队伍
     *
     * @param teamQuitRequesat
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequesat teamQuitRequesat, User loginUser);

    /**
     * 解散队伍
     *
     * @param teamId
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long teamId,User loginUser);

    /**
     * 获取队伍详细信息
     * @param id
     * @return
     */
    TeamVO getTeamDetail(Long id, Long userId);

    /**
     * 获取队伍成员信息
     * @param id
     * @return
     */
    List<TeamUserVO> getTeamMember(Long id);

    /**
     * 剔出队伍成员
     * @param teamId
     * @param userId
     * @param loginUser
     * @return
     */
    boolean kickTeamMember(Long teamId, Long userId, User loginUser);

    /**
     * 上传队伍头像
     * @param file
     * @param id
     * @param loginUser
     * @return
     */
    String uploadCover(MultipartFile file,Long id, User loginUser);
}
