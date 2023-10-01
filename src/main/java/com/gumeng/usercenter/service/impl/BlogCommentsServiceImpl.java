package com.gumeng.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.mapper.BlogCommentsMapper;
import com.gumeng.usercenter.model.domain.BlogComments;
import com.gumeng.usercenter.model.domain.CommentEmail;
import com.gumeng.usercenter.model.dto.Comments;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.dto.CommentEmailInfo;
import com.gumeng.usercenter.model.request.CommentAddRequest;
import com.gumeng.usercenter.model.vo.*;
import com.gumeng.usercenter.service.BlogCommentsService;
import com.gumeng.usercenter.service.BlogService;
import com.gumeng.usercenter.service.CommentEmailService;
import com.gumeng.usercenter.service.UserService;
import com.gumeng.usercenter.utils.EmailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gumeng.usercenter.contant.RedisConstant.*;

/**
 * @author 顾梦
 * @description 针对表【blog_comments(文章评论表)】的数据库操作Service实现
 * @createDate 2023-08-06 16:00:31
 */
@Service
@Slf4j
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments>
        implements BlogCommentsService {

    @Resource
    private BlogService blogService;

    @Resource
    private BlogCommentsMapper blogCommentsMapper;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CommentEmailService commentEmailService;

    @Override
    public List<CommentsVO> getCommentsByBlogId(Long blogId) {

        if (blogId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<Comments> comments = blogCommentsMapper.getCommentsByBlogId(blogId);
        List<CommentsVO> commentsVOList = comments.stream().map(comment -> {
            CommentsVO commentsVO = new CommentsVO();
            BeanUtils.copyProperties(comment, commentsVO);
            List<Comments> replyList = comment.getReplyList();
            if (replyList != null) {
                CommentReplyVO commentReplyVO = new CommentReplyVO();
                commentReplyVO.setList(replyList);
                commentReplyVO.setTotal(replyList.size());
                commentsVO.setReply(commentReplyVO);
            }
            return commentsVO;
        }).collect(Collectors.toList());

        return commentsVOList;
    }

    @Override
    public boolean likeComment(Long id, Long userId) {


        // 1.判断当前登录用户是否已经点赞
        String key = BLOG_COMMENT_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        BlogComments comments = getById(id);
        String likeNumKey = MESSAGE_LIKE_NUM_KEY + comments.getUserId();
        if (score == null) {
            // 2.如果未点赞，可以点赞
            // 2.1.数据库点赞数 + 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 2.2.保存用户到Redis到zset集合 zadd key value score
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
            Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
            if (!userId.equals(comments.getUserId())) {
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

    @Override
    @Transactional
    public CommentsVO addComment(CommentAddRequest commentAddRequest, User loginUser) {

        Long blogId = commentAddRequest.getBlogId();
        boolean isSuccess = blogService.update().setSql("comments = comments + 1").eq("id", blogId).update();
        if (!isSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论失败");
        }
        String content = commentAddRequest.getContent();
        Long parentId = commentAddRequest.getParentId();
        if (blogId == null || StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        MultipartFile[] files = commentAddRequest.getFiles();
        Long userId = loginUser.getId();
        BlogComments blogComments = new BlogComments();
        blogComments.setBlogId(blogId);
        blogComments.setContent(content);
        blogComments.setParentId(parentId);
        blogComments.setCreateTime(LocalDateTime.now());
        blogComments.setUserId(userId);
        // 查询评论和用户的信息作为模型数据
        CommentsVO reply = commentAddRequest.getReply();
        // 获取文章作者信息
        BlogVO blogVO = blogService.getBlogById(blogId, null);
        UserVO author = blogVO.getAuthor();
        Long commentUserId;
        if (reply != null) {
            blogComments.setAnswerId(reply.getUid());
            commentUserId = reply.getUid();
        }else {
            blogComments.setAnswerId(author.getId());
            commentUserId = author.getId();
        }
        String commentNumKey = MESSAGE_COMMENT_NUM_KEY + commentUserId;
        Boolean hasKey = stringRedisTemplate.hasKey(commentNumKey);
        if (Boolean.TRUE.equals(hasKey) && !Objects.equals(commentUserId, userId)) {
            stringRedisTemplate.opsForValue().increment(commentNumKey);
        } else {
            stringRedisTemplate.opsForValue().set(commentNumKey, "1");
        }

        boolean save = this.save(blogComments);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败");
        }

        // 定时任务发送邮件
        CommentEmail commentEmail = new CommentEmail();

        commentEmail.setPostName(blogVO.getTitle());
        // 上线时修改路径
        commentEmail.setPostUrl("https://yp.jishuqin.cn/blog?id=" + blogId);
        commentEmail.setComment(content);
        commentEmail.setNick(loginUser.getUsername());

        // 如果是回复评论发送邮件
        if (parentId != null && reply != null){

            Long uid = reply.getUid();
            User user = userService.getById(uid);
            commentEmail.setAvatar(user.getAvatarUrl());

            commentEmail.setEmail(user.getEmail());
            commentEmail.setParentComment(reply.getContent());
            commentEmail.setParentNick(user.getUsername());

            String subject =  commentEmail.getParentNick() + "，您在『悦配MATE』上的评论收到了回复";
            commentEmail.setSubject(subject);
        }else {
            // 封装评论邮件信息发送给作者
            commentEmail.setEmail(author.getEmail());
            commentEmail.setAvatar(author.getAvatarUrl());

            String subject =  author.getUsername() + "，您在『悦配MATE』上有新评论了";
            commentEmail.setSubject(subject);
        }
        commentEmailService.save(commentEmail);

        return getCommentsById(blogComments.getId());
    }


    @Override
    public CommentsVO getCommentsById(Long id) {

        Comments comments = blogCommentsMapper.getCommentsById(id);

        CommentsVO commentsVO = new CommentsVO();
        BeanUtils.copyProperties(comments, commentsVO);
        List<Comments> replyList = comments.getReplyList();
        if (replyList != null) {
            CommentReplyVO commentReplyVO = new CommentReplyVO();
            commentReplyVO.setList(replyList);
            commentReplyVO.setTotal(replyList.size());
            commentsVO.setReply(commentReplyVO);
        }
        return commentsVO;
    }

    @Override
    public CommentUserVO getCommentUser(Long blogId, User loginUser) {

        Long userId = loginUser.getId();
        CommentUserVO commentUser = new CommentUserVO();
        commentUser.setId(userId);
        commentUser.setUsername(loginUser.getUsername());
        commentUser.setAvatar(loginUser.getAvatarUrl());
        commentUser.setHomeLink("/user/detail?id=" + userId);

        List<BlogComments> comments = query().eq("blogId", blogId).list();

        List<Long> likeIds = comments.stream().map(coment -> {
            Long id = coment.getId();
            String key = BLOG_COMMENT_LIKED_KEY + id;
            Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
            return score != null ? id : null;
        }).collect(Collectors.toList());
        commentUser.setLikeIds(likeIds);

        return commentUser;
    }

    @Override
    @Transactional
    public boolean deleteComments(CommentsVO comments,Long blogId, User loginUser) {

        // 校验参数
        if (comments == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(loginUser);
        Long userId = loginUser.getId();

        // 校验用户权限，仅管理员或评论者可删除评论
        if (!userId.equals(comments.getUid()) && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }


        Long id = comments.getId();
        Long parentId = comments.getParentId();

        // 如果是二级评论直接删除
        boolean remove = removeById(id);
        boolean isSuccess = blogService.update().setSql("comments = comments - 1")
                .ne("comments",0)
                .eq("id", blogId).update();
        if (!remove || !isSuccess){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        String key = BLOG_COMMENT_LIKED_KEY + id;
        // 删除用户点赞数据
        stringRedisTemplate.delete(key);

        // 如果是一级评论删除其下二级评论
        if (parentId == null){
            List<BlogComments> requeryList = query().eq("parentId", id).list();

            if (requeryList != null && requeryList.size() > 0){
                for (BlogComments comment : requeryList) {
                    // 删除用户点赞数据
                    key = BLOG_COMMENT_LIKED_KEY + comment.getId();
                    stringRedisTemplate.delete(key);
                    // 删除 blog_comments 表中数据
                    removeById(comment);
                }
                boolean update = blogService.update().setSql("comments = comments - " + requeryList.size())
                        .ne("comments", 0)
                        .eq("id", blogId).update();
                if (!update){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
            }
        }
        return true;
    }

    @Override
    public List<BlogCommentsVO> getMyComments(Long userId ) {

        List<BlogCommentsVO> myComments = blogCommentsMapper.getMyComments(userId);

        myComments.forEach(c ->{
            Comments comment = c.getComment();
            Long id = comment.getId();
            // 1.判断当前登录用户是否已经点赞
            String key = BLOG_COMMENT_LIKED_KEY + id;
            Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
            c.setIsLike(score != null);

            BlogVO blog = c.getBlog();
            String images = blog.getImages();
            if (StringUtils.isNotBlank(images)){
                String[] imagesUrl = images.split(",");
                blog.setCoverImage(imagesUrl[0]);
            }

        });
        String commentNumKey = MESSAGE_COMMENT_NUM_KEY + userId;
        stringRedisTemplate.delete(commentNumKey);
        return myComments;
    }

}




