package com.gumeng.usercenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.usercenter.common.PageParams;
import com.gumeng.usercenter.common.PageResult;
import com.gumeng.usercenter.model.domain.Blog;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.dto.ScrollResult;
import com.gumeng.usercenter.model.request.BlogAddRequest;
import com.gumeng.usercenter.model.request.BlogUpdateRequest;
import com.gumeng.usercenter.model.vo.BlogVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 顾梦
* @description 针对表【blog(文章表)】的数据库操作Service
* @createDate 2023-08-06 16:00:31
*/
public interface BlogService extends IService<Blog> {

    /**
     * 添加文章
     * @param blogAddRequest
     * @return
     */
    boolean addBlog(BlogAddRequest blogAddRequest,Long userId);

    /**
     * 点赞文章
     * @param id
     * @return
     */
    boolean likeBlog(Long id,Long userId);

    /**
     * 分页查询文章
     * @param pageParams
     * @return
     */
    PageResult<BlogVO> listBlog(PageParams pageParams,String title,Long userId);

    /**
     * 分页查询我的文章
     * @param pageParams
     * @param userId
     * @return
     */
    PageResult<BlogVO> listMyBlog(PageParams pageParams, Long userId);

    /**
     * 根据id查询文章
     * @param id
     * @param userId
     * @return
     */
    BlogVO getBlogById(long id, Long userId);

    /**
     * 根据id修改文章
     * @param blogUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateBlogById(BlogUpdateRequest blogUpdateRequest,User loginUser);

    /**
     * 根据id删除文章
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteBlogById(Long id, User loginUser);

    /**
     * 获取关注用户文章
     * @param max
     * @param offset
     * @param userId
     * @return
     */
    ScrollResult getBlogOfFollow(Long max, Integer offset, Long userId);

}
