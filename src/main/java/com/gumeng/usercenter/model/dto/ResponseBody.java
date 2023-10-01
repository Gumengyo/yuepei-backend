package com.gumeng.usercenter.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author 顾梦
 * @description
 * @since 2023/8/19
 */
@Data
public class ResponseBody {
    private List<Choices> choices;
}
