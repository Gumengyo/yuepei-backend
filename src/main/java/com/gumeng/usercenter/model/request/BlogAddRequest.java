package com.gumeng.usercenter.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 顾梦
 * @description 添加文章封装类
 * @since 2023/8/6
 */
@Data
public class BlogAddRequest implements Serializable{

    /**
     * 标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章图片
     */
    private MultipartFile[] images;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
