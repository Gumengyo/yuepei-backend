package com.gumeng.usercenter.service;

import com.gumeng.usercenter.model.domain.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.model.request.TagAddRequest;

/**
* @author 顾梦
* @description 针对表【tag(标签)】的数据库操作Service
* @createDate 2023-07-23 19:14:22
*/
public interface TagService extends IService<Tag> {

    /**
     * 添加标签
     * @param tagAddRequest
     * @return
     */
    boolean addTagsName(TagAddRequest tagAddRequest, User loginUser);

}
