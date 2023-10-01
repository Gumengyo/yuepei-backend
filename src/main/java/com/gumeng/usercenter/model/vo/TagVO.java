package com.gumeng.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 顾梦
 * @description 标签包装类
 * @since 2023/8/3
 */
@Data
public class TagVO implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * 标签名称
     */
    private String text;


    private static final long serialVersionUID = 1L;

    List<TagVO> children;
}
