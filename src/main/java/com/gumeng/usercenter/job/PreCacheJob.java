package com.gumeng.usercenter.job;

import com.gumeng.usercenter.common.PageParams;
import com.gumeng.usercenter.common.PageResult;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.vo.UserVO;
import com.gumeng.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.gumeng.usercenter.contant.RedisConstant.DO_CACHE_JOB_LOCK;
import static com.gumeng.usercenter.contant.RedisConstant.USER_MATCH;

/**
 * @author 顾梦
 * @description 缓存预热任务
 * @since 2023/7/27
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;


    // 每天早上6点执行，预热用户推荐信息
    @Scheduled(cron = "0 0 6 * * ?")
    public void doCacheRecommendUser() {
        // 获得锁
        RLock lock = redissonClient.getLock(DO_CACHE_JOB_LOCK);
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                PageParams pageParams = new PageParams(1, 20);
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                List<User> userList = userService.list();

                for (User user : userList) {

                    // 用户未添加标签，无法匹配
                    String tags = user.getTags();
                    if (StringUtils.isBlank(tags) || tags.equals("[]")){
                        continue;
                    }

                    String redisKey = String.format(USER_MATCH,user.getId());
                    List<UserVO> matchUsers = userService.matchUsers(20, user);

                    // 写到缓存
                    try {
                        valueOperations.set(redisKey, matchUsers, 6, TimeUnit.HOURS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }

            }
        } catch (Exception e) {
            log.error("doCacheRecommendUser error",e);
        }finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

}