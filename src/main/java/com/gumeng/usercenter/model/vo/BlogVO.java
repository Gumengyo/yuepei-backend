package com.gumeng.usercenter.model.vo;

import com.gumeng.usercenter.model.domain.Blog;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 文章包装类
 *
 * @author 顾梦
 * @date 2023/06/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BlogVO extends Blog implements Serializable {

    /**
     * 串行版本uid
     */
    private static final long serialVersionUID = -1461567317259590205L;

    /**
     * 是否点赞
     */
    private Boolean isLike;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 作者
     */
    private UserVO author;
}
