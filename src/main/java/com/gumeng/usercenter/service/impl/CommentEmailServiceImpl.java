package com.gumeng.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.usercenter.model.domain.CommentEmail;
import com.gumeng.usercenter.service.CommentEmailService;
import com.gumeng.usercenter.mapper.CommentEmailMapper;
import org.springframework.stereotype.Service;

/**
* @author 顾梦
* @description 针对表【comment_email(评论邮件)】的数据库操作Service实现
* @createDate 2023-08-22 14:47:19
*/
@Service
public class CommentEmailServiceImpl extends ServiceImpl<CommentEmailMapper, CommentEmail>
    implements CommentEmailService{

}




