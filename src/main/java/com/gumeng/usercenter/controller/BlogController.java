package com.gumeng.usercenter.controller;

import com.gumeng.usercenter.common.*;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.dto.ScrollResult;
import com.gumeng.usercenter.model.request.BlogAddRequest;
import com.gumeng.usercenter.model.request.BlogUpdateRequest;
import com.gumeng.usercenter.model.vo.BlogVO;
import com.gumeng.usercenter.service.BlogService;
import com.gumeng.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 顾梦
 * @description 文章管理接口
 * @since 2023/8/6
 */
@RestController
@RequestMapping("/blog")
@Api(tags = "文章管理接口")
@Slf4j
public class BlogController {

    @Resource
    private BlogService blogService;

    @Resource
    private UserService userService;

    /**
     * 添加文章
     * @param blogAddRequest
     * @param request
     * @return
     */
    @ApiOperation(value = "添加文章")
    @PostMapping("/add")
    public BaseResponse<Boolean> addBlog(BlogAddRequest blogAddRequest,
                                         HttpServletRequest request) {

        if (blogAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        boolean added = blogService.addBlog(blogAddRequest, userId);

        if (!added) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败");
        }

        return ResultUtils.success(true);
    }

    /**
     * 点赞文章
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "点赞文章")
    @PutMapping("/like/{id}")
    public BaseResponse<Boolean> likeBlog(@PathVariable("id") Long id, HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        boolean liked = blogService.likeBlog(id, userId);
        return ResultUtils.success(liked);
    }

    /**
     * 分页获取文章列表
     * @param pageParams
     * @param title
     * @param request
     * @return
     */
    @ApiOperation(value = "分页获取文章列表")
    @GetMapping("/list")
    public BaseResponse<PageResult<BlogVO>> listBlog(PageParams pageParams, String title, HttpServletRequest request) {

        if (pageParams == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = null;
        try {
            User loginUser = userService.getLoginUser(request);
            userId = loginUser.getId();
        } catch (Exception ignored) {
        }

        PageResult<BlogVO> pageResult = blogService.listBlog(pageParams, title, userId);
        return ResultUtils.success(pageResult);
    }

    /**
     * 分页获取我的文章列表
     * @param pageParams
     * @param request
     * @return
     */
    @ApiOperation(value = "分页获取我的文章列表")
    @GetMapping("/list/my/blog")
    public BaseResponse<PageResult<BlogVO>> listMyBlog(PageParams pageParams, HttpServletRequest request) {

        if (pageParams == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        PageResult<BlogVO> pageResult = blogService.listMyBlog(pageParams, userId);
        return ResultUtils.success(pageResult);
    }

    /**
     * 根据 id 获取文章
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "根据 id 获取文章")
    @GetMapping("/{id}")
    public BaseResponse<BlogVO> getBlogById(@PathVariable("id") long id, HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        BlogVO blogVO = blogService.getBlogById(id, userId);

        return ResultUtils.success(blogVO);
    }

    /**
     * 更新文章
     * @param blogUpdateRequest
     * @param request
     * @return
     */
    @ApiOperation(value = "更新文章")
    @PutMapping("/update")
    public BaseResponse<Boolean> updateBlogById(BlogUpdateRequest blogUpdateRequest,HttpServletRequest request){

        if (blogUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        boolean updated = blogService.updateBlogById(blogUpdateRequest,loginUser);
        return ResultUtils.success(updated);
    }

    /**
     * 删除文章
     * @param id
     * @param request
     * @return
     */
    @ApiOperation(value = "删除文章")
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteBlogById(@PathVariable("id") Long id, HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);

        boolean deleted = blogService.deleteBlogById(id,loginUser);

        return ResultUtils.success(deleted);
    }

    /**
     * 分页获取关注的人的文章
     * @param request
     * @param max
     * @param offset
     * @return
     */
    @ApiOperation(value = "分页获取关注的人的文章")
    @GetMapping("/of/follow")
    public BaseResponse<ScrollResult> getBlogOfFollow(HttpServletRequest request,@RequestParam("lastId") Long max,
                                                        @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        ScrollResult blogOfFollow = blogService.getBlogOfFollow(max, offset, userId);
        return ResultUtils.success(blogOfFollow);
    }
}















