package com.gumeng.usercenter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 顾梦
 * @description
 * @since 2023/8/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GptMessage {
    private String role;
    private String content;

}
