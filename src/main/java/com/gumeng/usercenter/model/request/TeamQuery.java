package com.gumeng.usercenter.model.request;

import lombok.Data;

import java.util.List;

/**
 * @author 顾梦
 * @description 队伍查询类
 * @since 2023/7/28
 */
@Data
public class TeamQuery {
    /**
     * id
     */
    private Long id;

    /**
     * id 列表
     */
    private List<Long> idList;


    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

}
