package com.gumeng.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.model.domain.Tag;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.request.TagAddRequest;
import com.gumeng.usercenter.service.TagService;
import com.gumeng.usercenter.mapper.TagMapper;
import com.gumeng.usercenter.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
* @author 顾梦
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-07-23 19:14:22
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

    @Resource
    private UserService userService;

    @Override
    public boolean addTagsName(TagAddRequest tagAddRequest, User loginUser) {

        if (tagAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean admin = userService.isAdmin(loginUser);
        // 校验身份，管理员才可以添加
        if (!admin){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        tagAddRequest.setUserId(loginUser.getId());

        Tag tag = new Tag();
        BeanUtils.copyProperties(tagAddRequest,tag);

        if (tag.getParentId() != null){
            tag.setIsParent(0);
        }else {
            tag.setIsParent(1);
        }
        tag.setCreateTime(LocalDateTime.now());

        return save(tag);
    }

}




