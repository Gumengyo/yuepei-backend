package com.gumeng.usercenter.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.common.PageParams;
import com.gumeng.usercenter.common.PageResult;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.mapper.BlogMapper;
import com.gumeng.usercenter.model.domain.Blog;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.dto.ScrollResult;
import com.gumeng.usercenter.model.request.BlogAddRequest;
import com.gumeng.usercenter.model.request.BlogUpdateRequest;
import com.gumeng.usercenter.model.vo.BlogVO;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.BlogService;
import com.gumeng.usercenter.service.FollowService;
import com.gumeng.usercenter.service.UserService;
import com.gumeng.usercenter.utils.QiniuUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.gumeng.usercenter.contant.CommonConstant.QINIU_DOMAIN;
import static com.gumeng.usercenter.contant.RedisConstant.*;

/**
 * @author 顾梦
 * @description 针对表【blog(文章表)】的数据库操作Service实现
 * @createDate 2023-08-06 16:00:31
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private FollowService followService;

    /**
     * 添加文章
     *
     * @param blogAddRequest
     * @return
     */
    @Override
    public boolean addBlog(BlogAddRequest blogAddRequest, Long userId) {

        if (blogAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 限制文章字数
        String content = blogAddRequest.getContent();
        String title = blogAddRequest.getTitle();
        if (StringUtils.isAnyBlank(content, title)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题或正文为空");
        }

        if (title.length() > 50 || content.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "字数超过限制");
        }

        MultipartFile[] images = blogAddRequest.getImages();
        String imagesUrl = uploadImages(images);

        Blog blog = new Blog();
        BeanUtils.copyProperties(blogAddRequest, blog);
        blog.setUserId(userId);
        blog.setCreateTime(LocalDateTime.now());
        blog.setImages(imagesUrl);
        boolean saved = this.save(blog);
        // 查询作者的所有粉丝
        List<UserVO> myFans = followService.getMyFans(userId);
        // 推送文章id给所有粉丝
        for (UserVO fan : myFans) {
            // 获取粉丝id
            Long fanId = fan.getId();
            // 推送
            String key = FEED_KEY + fanId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
            String likeNumKey = MESSAGE_BLOG_NUM_KEY + fanId;
            Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
            if (Boolean.TRUE.equals(hasKey)) {
                stringRedisTemplate.opsForValue().increment(likeNumKey);
            } else {
                stringRedisTemplate.opsForValue().set(likeNumKey, "1");
            }
        }

        return saved;
    }

    @Override
    public PageResult<BlogVO> listBlog(PageParams pageParams, String title, Long userId) {

        if (pageParams == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long pageNum = pageParams.getPageNum();
        Long pageSize = pageParams.getPageSize();
        if (pageSize > 10 || pageSize < 0 || pageNum < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.orderByDesc("liked");

        Page<Blog> blogPage = new Page<>(pageNum, pageSize);
        blogPage = this.page(blogPage, queryWrapper);

        List<Blog> blogList = blogPage.getRecords();
        List<BlogVO> blogVOList = blogList.stream().map(blog -> {
            BlogVO blogVO = new BlogVO();
            BeanUtils.copyProperties(blog, blogVO);
            if (userId != null) {
                // 判断当前用户是否点赞过文章
                String key = BLOG_LIKED_KEY + blog.getId();
                Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
                blogVO.setIsLike(score != null);
            }
            String images = blogVO.getImages();
            if (images != null) {
                String[] imagesUrl = images.split(",");
                blogVO.setCoverImage(imagesUrl[0]);
            }
            return blogVO;
        }).collect(Collectors.toList());

        return new PageResult<>(blogVOList, blogPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<BlogVO> listMyBlog(PageParams pageParams, Long userId) {

        if (pageParams == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long pageNum = pageParams.getPageNum();
        Long pageSize = pageParams.getPageSize();
        if (pageSize > 10 || pageSize < 0 || pageNum < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userId != null, "userId", userId);

        Page<Blog> blogPage = new Page<>(pageNum, pageSize);
        blogPage = this.page(blogPage, queryWrapper);

        List<Blog> blogList = blogPage.getRecords();
        List<BlogVO> blogVOList = blogList.stream().map(blog -> {
            BlogVO blogVO = new BlogVO();
            BeanUtils.copyProperties(blog, blogVO);
            // 判断当前用户是否点赞过文章
            String key = BLOG_LIKED_KEY + blog.getId();
            Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
            blogVO.setIsLike(score != null);
            String images = blogVO.getImages();
            if (images != null) {
                String[] imagesUrl = images.split(",");
                blogVO.setCoverImage(imagesUrl[0]);
            }
            return blogVO;
        }).collect(Collectors.toList());

        return new PageResult<>(blogVOList, blogPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public BlogVO getBlogById(long id, Long userId) {

        Blog blog = this.getById(id);
        if (blog == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);

        if (userId != null) {
            // 判断当前用户是否点赞过文章
            String key = BLOG_LIKED_KEY + blog.getId();
            Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
            blogVO.setIsLike(score != null);
        }

        String images = blogVO.getImages();
        if (images != null) {
            String[] imagesUrl = images.split(",");
            blogVO.setCoverImage(imagesUrl[0]);
        }

        Long authorId = blogVO.getUserId();
        UserVO userVO = userService.getUserById(authorId, userId);
        blogVO.setAuthor(userVO);

        return blogVO;
    }

    @Override
    public boolean updateBlogById(BlogUpdateRequest blogUpdateRequest, User loginUser) {

        // 校验参数
        if (blogUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = blogUpdateRequest.getId();
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Blog blog = this.getById(id);
        if (blog == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "文章不存在");
        }

        // 判断文章作者是否为当前用户
        // 仅文章作者或管理员可以修改
        boolean isAdmin = userService.isAdmin(loginUser);
        Long userId = loginUser.getId();

        if (!userId.equals(blog.getUserId()) || !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 限制文章字数
        String title = blogUpdateRequest.getTitle();
        String content = blogUpdateRequest.getContent();

        if (title.length() > 50 || content.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "字数超过限制");
        }

        String imgStr = blogUpdateRequest.getImgStr();

        // 上传已更改的图片、删除原有图片
        String oldStr = blog.getImages();
        if (StringUtils.isNotBlank(oldStr)) {
            String[] oldImage = getImageName(oldStr);
            for (String name : oldImage) {
                if (!imgStr.contains(name) || StringUtils.isBlank(imgStr)) {
                    QiniuUtils.deleteFileFromQiniu(name);
                }
            }
        }

        MultipartFile[] images = blogUpdateRequest.getImages();
        // 若无上传新图片、无需更改原有图片
        if (images != null && images.length != 0) {
            String newImgStr = uploadImages(images);
            if (StringUtils.isNotBlank(imgStr)) {
                blog.setImages(imgStr + "," + newImgStr);
            } else {
                blog.setImages(newImgStr);
            }
        } else {
            blog.setImages(imgStr);
        }

        blog.setTitle(title);
        blog.setContent(content);
        blog.setLiked(null);
        blog.setComments(null);
        blog.setUpdateTime(LocalDateTime.now());

        // 修改blog表内容
        return this.updateById(blog);
    }

    @Override
    @Transactional
    public boolean deleteBlogById(Long id, User loginUser) {

        Blog blog = this.getById(id);
        if (blog == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "文章不存在");
        }

        boolean isAdmin = userService.isAdmin(loginUser);
        Long userId = blog.getUserId();
        // 只用管理员或作者才可以删除文章
        if (!userId.equals(loginUser.getId()) || !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 删除文章图片
        String images = blog.getImages();
        String[] imageName = getImageName(images);

        for (String name : imageName) {
            QiniuUtils.deleteFileFromQiniu(name);
        }

        // 删除文章点赞数据
        String likedKey = BLOG_LIKED_KEY + id;
        stringRedisTemplate.delete(likedKey);

        // 删除 blog 和 blog_comment 关联数据
        boolean remove = this.removeById(id);

        // 查询作者的所有粉丝
        List<UserVO> myFans = followService.getMyFans(userId);
        // 删除给所有粉丝的这篇文章
        for (UserVO fan : myFans) {
            // 获取粉丝id
            Long fanId = fan.getId();
            // 推送
            String feedKey = FEED_KEY + fanId;
            stringRedisTemplate.opsForZSet().remove(feedKey, blog.getId().toString());
        }

        return remove;
    }

    @Override
    public ScrollResult getBlogOfFollow(Long max, Integer offset, Long userId) {

        // 1.查询收件箱 ZREVRANGEBYSCORE key Max Min LIMIT offset count
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 6);
        ScrollResult r = new ScrollResult();
        // 2.非空判断
        if (typedTuples == null || typedTuples.isEmpty()) {
            r.setList(Collections.emptyList());
            return r;
        }
        // 3.解析数据：blogId、minTime（时间戳）、offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            // 3.1.获取id
            ids.add(Long.valueOf(tuple.getValue()));
            // 3.2.获取分数(时间戳）
            long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }
        os = minTime == max ? os : os + offset;
        // 5.根据id查询blog
        List<BlogVO> blogs = ids.stream().map(id -> getBlogById(id, userId)).collect(Collectors.toList());

        // 6.封装并返回
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);

        // 7.删除文章消息提示
        String likeNumKey = MESSAGE_BLOG_NUM_KEY + userId;
        stringRedisTemplate.delete(likeNumKey);

        return r;
    }

    @Override
    public boolean likeBlog(Long id, Long userId) {

        // 1.判断当前登录用户是否已经点赞
        String key = BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        Blog blog = getById(id);
        String likeNumKey = MESSAGE_LIKE_NUM_KEY + blog.getUserId();
        if (score == null) {
            // 2.如果未点赞，可以点赞
            // 2.1.数据库点赞数 + 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 2.2.保存用户到Redis到zset集合 zadd key value score
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }

            Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
            if (!Objects.equals(blog.getUserId(), userId)) {
                if (Boolean.TRUE.equals(hasKey)) {
                    stringRedisTemplate.opsForValue().increment(likeNumKey);
                } else {
                    stringRedisTemplate.opsForValue().set(likeNumKey, "1");
                }
            }

        } else {
            // 3.如果已点赞，取消点赞
            // 3.1.数据库点赞数 -1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 3.2.把用户从Redis的set集合移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }

        return true;
    }

    private String[] getImageName(String imgStr) {
        return imgStr.replaceAll(QINIU_DOMAIN, "").split(",");
    }

    private String uploadImages(MultipartFile[] images) {

        StringBuilder imagesUrl = new StringBuilder();

        if (images == null) {
            return imagesUrl.toString();
        }


        for (MultipartFile image : images) {
            // 原始文件名
            String originalFilename = image.getOriginalFilename();
            if (originalFilename == null) {
                return imagesUrl.toString();
            }
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
            String fileName = IdUtil.simpleUUID() + suffix;

            try {
                // 将文件上传到七牛云服务器
                QiniuUtils.upload2Qiniu(image.getBytes(), fileName);
                imagesUrl.append(QINIU_DOMAIN).append(fileName)
                        .append(',');
            } catch (IOException e) {
                e.printStackTrace();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败");
            }
        }
        int lastIndex = imagesUrl.lastIndexOf(",");
        imagesUrl.deleteCharAt(lastIndex);
        return imagesUrl.toString();
    }

}




