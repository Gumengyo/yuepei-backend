package com.gumeng.usercenter.common;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.ToString;

/**
 *
 * @author 顾梦
 * @since 2023/7/11
 * @description 分页查询通用参数
 */
@Data
@ToString
public class PageParams {

    //当前页码
    @Parameter(name = "当前页码")
    private Long pageNum = 1L;

    //每页记录数默认值
    @Parameter(name = "每页记录数默认值")
    private Long pageSize =10L;

    public PageParams(){

    }

    public PageParams(long pageNum,long pageSize){
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

}
