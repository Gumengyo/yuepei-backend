package com.gumeng.usercenter.job;

import com.gumeng.usercenter.model.domain.CommentEmail;
import com.gumeng.usercenter.service.CommentEmailService;
import com.gumeng.usercenter.utils.EmailService;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.gumeng.usercenter.contant.RedisConstant.SEND_EMAIL_JOB_LOCK;

/**
 * @author 顾梦
 * @description 定时邮件发送
 * @since 2023/8/22
 */
@Slf4j
@Component
public class SendEmailJob {

    @Resource
    private CommentEmailService commentEmailService;

    @Resource
    private EmailService emailService;

    @Resource
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 */15 * * * ?") // 每15分钟执行任务
    public void sendCommentEmails() {
        // 获得锁
        RLock lock = redissonClient.getLock(SEND_EMAIL_JOB_LOCK);

        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                List<CommentEmail> commentEmails = commentEmailService.query().lt("failNum", 3)
                        .orderByAsc("createTime")
                        .list();
                if (commentEmails.isEmpty()){
                    return;
                }
                commentEmails.forEach(commentEmail -> {
                    String emailHtml = generateCommentEmailHtml(commentEmail);
                    if (emailHtml != null) {
                        // 邮件模板生成成功 发送邮件
                        emailService.sendHtmlMail(commentEmail.getEmail(), commentEmail.getSubject(), emailHtml);
                        commentEmailService.removeById(commentEmail);
                    } else {
                        commentEmailService.update().setSql("failNum = failNum + 1").eq("id", commentEmail.getId()).update();
                        ;
                    }
                });
            }
        }catch (Exception e){
            log.error("sendEmailJob error",e);
        }finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

    }

    private String generateCommentEmailHtml(CommentEmail commentEmail) {

        //配置freemarker
        Configuration configuration = new Configuration(Configuration.getVersion());

        try {
            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            configuration.setTemplateLoader(new ClassTemplateLoader(getClass(), "/templates"));

            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("mail_template.ftl");

            Map<String, Object> map = new HashMap<>();
            map.put("model", commentEmail);

            //静态化
            //参数1：模板，参数2：数据模型
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        } catch (Exception e) {
            log.error("邮件模板静态化出现问题,课程id:{}", commentEmail, e);
        }

        return null;
    }


}
