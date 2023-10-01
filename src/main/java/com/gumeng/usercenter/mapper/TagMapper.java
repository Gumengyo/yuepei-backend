package com.gumeng.usercenter.mapper;

import com.gumeng.usercenter.model.domain.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gumeng.usercenter.model.vo.TagVO;

import java.util.List;

/**
* @author 顾梦
* @description 针对表【tag(标签)】的数据库操作Mapper
* @createDate 2023-07-23 19:14:22
* @Entity generator.domain.Tag
*/
public interface TagMapper extends BaseMapper<Tag> {
    /**
     * 获取全部标签
     * @return
     */
    List<TagVO> getTagList();
}




