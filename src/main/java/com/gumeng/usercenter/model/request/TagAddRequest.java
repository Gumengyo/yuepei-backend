package com.gumeng.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 顾梦
 * @description 添加标签
 * @since 2023/8/3
 */
@Data
public class TagAddRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 父标签 id
     */
    private Long parentId;

}
