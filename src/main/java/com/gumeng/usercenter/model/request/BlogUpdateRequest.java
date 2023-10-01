package com.gumeng.usercenter.model.request;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * @author 顾梦
 * @description 文章修改
 * @since 2023/8/7
 */
@Data
public class BlogUpdateRequest implements Serializable {

    /**
     * 文章id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 未上传的图片
     */
    private MultipartFile[] images;

    /**
     * 已上传的图片
     */
    private String imgStr;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}