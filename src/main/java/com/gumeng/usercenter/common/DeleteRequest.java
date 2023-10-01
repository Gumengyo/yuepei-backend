package com.gumeng.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 顾梦
 * @description 通用的删除请求
 * @since 2023/7/31
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;

    private long id;
}
