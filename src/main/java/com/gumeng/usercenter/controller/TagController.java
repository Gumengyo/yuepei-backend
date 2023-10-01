package com.gumeng.usercenter.controller;

import com.gumeng.usercenter.common.BaseResponse;
import com.gumeng.usercenter.common.ResultUtils;
import com.gumeng.usercenter.mapper.TagMapper;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.request.TagAddRequest;
import com.gumeng.usercenter.model.vo.TagVO;
import com.gumeng.usercenter.service.TagService;
import com.gumeng.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 顾梦
 * @description 标签接口
 * @since 2023/8/3
 */
@Api(tags = "标签信息管理接口")
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    TagService tagService;

    @Resource
    TagMapper tagMapper;

    @Resource
    UserService userService;

    /**
     * 获取标签列表
     * @return
     */
    @ApiOperation(value = "获取标签列表")
    @GetMapping("/list")
    public BaseResponse<List<TagVO>> getTagList(){
        List<TagVO> tagList = tagMapper.getTagList();
        return ResultUtils.success(tagList);
    }

    /**
     * 添加标签
     * @param tagAddRequest
     * @param request
     * @return
     */
    @ApiOperation(value = "添加标签")
    @GetMapping("/add")
    public BaseResponse<Boolean> addTag(TagAddRequest tagAddRequest, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        boolean added = tagService.addTagsName(tagAddRequest, loginUser);
        return ResultUtils.success(added);
    }
}
