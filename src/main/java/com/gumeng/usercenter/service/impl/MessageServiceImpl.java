package com.gumeng.usercenter.service.impl;

import com.gumeng.usercenter.model.domain.Blog;
import com.gumeng.usercenter.model.domain.BlogComments;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.dto.Message;
import com.gumeng.usercenter.model.vo.BlogVO;
import com.gumeng.usercenter.model.vo.CommentsVO;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.BlogCommentsService;
import com.gumeng.usercenter.service.BlogService;
import com.gumeng.usercenter.service.MessageService;
import com.gumeng.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gumeng.usercenter.contant.RedisConstant.CACHE_CHAT_PRIVATE;
import static com.gumeng.usercenter.contant.RedisConstant.*;
import static com.gumeng.usercenter.contant.RedisConstant.MESSAGE_LIKE_SET_KEY;

/**
 * @author 顾梦
 * @description 消息获取
 * @since 2023/8/12
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private BlogService blogService;

    @Resource
    private BlogCommentsService blogCommentsService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public List<Message> getMyLikeMessage(User loginUser) {

        Long userId = loginUser.getId();

        // 存储数据
        RScoredSortedSet<Message> scoredSortedSet = redissonClient.getScoredSortedSet(MESSAGE_LIKE_SET_KEY + userId);
        ZSetOperations<String, String> zSetOps = stringRedisTemplate.opsForZSet();
        // 获取我发布的文章
        List<Blog> blogList = blogService.query().eq("userId", userId).list();
        if (blogList != null && !blogList.isEmpty()) {
            UserVO author = new UserVO();
            BeanUtils.copyProperties(loginUser, author);
            for (Blog blog : blogList) {
                Long blogId = blog.getId();
                String key = BLOG_LIKED_KEY + blogId;
                Set<String> userIdSet = zSetOps.range(key, 0, -1);
                if (userIdSet == null || userIdSet.isEmpty()) {
                    continue;
                }
                List<Long> userIds = userIdSet.stream().map(Long::valueOf).collect(Collectors.toList());
                for (Long uid : userIds) {
                    if (Objects.equals(uid, userId)) {
                        continue;
                    }
                    Message message = new Message();
                    message.setType(0);
                    UserVO userVO = userService.getUserById(uid, null);
                    message.setFromUser(userVO);
                    BlogVO blogVO = new BlogVO();
                    BeanUtils.copyProperties(blog, blogVO);
                    String images = blog.getImages();
                    if (StringUtils.isNotBlank(images)) {
                        String[] imagesUrl = images.split(",");
                        blogVO.setCoverImage(imagesUrl[0]);
                    }

                    blogVO.setAuthor(author);
                    message.setBlog(blogVO);
                    Double timestamp = zSetOps.score(key, uid.toString());
                    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.longValue()), ZoneId.systemDefault());
                    message.setCreateTime(dateTime);
                    scoredSortedSet.add(timestamp, message);
                }
            }
        }

        // 获取我发布的评论
        List<BlogComments> comments = blogCommentsService.query().eq("userId", userId).list();
        if (comments != null && !comments.isEmpty()) {
            for (BlogComments comment : comments) {
                Long commentId = comment.getId();
                String key = BLOG_COMMENT_LIKED_KEY + commentId;
                Set<String> userIdSet = zSetOps.range(key, 0, -1);
                if (userIdSet == null || userIdSet.isEmpty()) {
                    continue;
                }
                List<Long> userIds = userIdSet.stream().map(Long::valueOf).collect(Collectors.toList());
                for (Long uid : userIds) {
                    if (Objects.equals(uid, userId)) {
                        continue;
                    }
                    Message message = new Message();
                    message.setType(1);
                    CommentsVO commentsVO = blogCommentsService.getCommentsById(commentId);
                    message.setComment(commentsVO);
                    UserVO userVO = userService.getUserById(uid, null);
                    message.setFromUser(userVO);
                    Double timestamp = zSetOps.score(key, uid.toString());
                    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.longValue()), ZoneId.systemDefault());
                    message.setCreateTime(dateTime);
                    scoredSortedSet.add(timestamp, message);
                }
            }
        }

        Collection<ScoredEntry<Message>> scoredEntries = scoredSortedSet.entryRangeReversed(0, -1);
        List<Message> messageList = scoredEntries.stream().map(ScoredEntry::getValue).collect(Collectors.toList());
        stringRedisTemplate.delete(MESSAGE_LIKE_SET_KEY + userId);
        stringRedisTemplate.delete(MESSAGE_LIKE_NUM_KEY + userId);
        return messageList;
    }

    @Override
    public Long getCommentNum(Long userId) {
        String commentNumKey = MESSAGE_COMMENT_NUM_KEY + userId;
        Boolean hasKey = stringRedisTemplate.hasKey(commentNumKey);
        if (Boolean.TRUE.equals(hasKey)) {
            String commentNum = stringRedisTemplate.opsForValue().get(commentNumKey);
            return commentNum != null ? Long.parseLong(commentNum) : 0L;
        } else {
            return 0L;
        }
    }

    @Override
    public Long getLikeNum(Long userId) {
        String likeNumKey = MESSAGE_LIKE_NUM_KEY + userId;
        Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
        if (Boolean.TRUE.equals(hasKey)) {
            String commentNum = stringRedisTemplate.opsForValue().get(likeNumKey);
            return commentNum != null ? Long.parseLong(commentNum) : 0L;
        } else {
            return 0L;
        }
    }

    @Override
    public Long getChatNum(Long userId) {
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
        String key = CACHE_CHAT_PRIVATE + userId;
        List<String> values = hashOperations.values(key);
        long count = 0L;
        for (String value : values) {
            count += Long.parseLong(value);
        }
        return count;
    }

    @Override
    public Long getBlogOfFollowNum(Long userId) {
        String likeNumKey = MESSAGE_BLOG_NUM_KEY + userId;
        Boolean hasKey = stringRedisTemplate.hasKey(likeNumKey);
        if (Boolean.TRUE.equals(hasKey)) {
            String followNum = stringRedisTemplate.opsForValue().get(likeNumKey);
            return followNum != null ? Long.parseLong(followNum) : 0L;
        } else {
            return 0L;
        }
    }
}
