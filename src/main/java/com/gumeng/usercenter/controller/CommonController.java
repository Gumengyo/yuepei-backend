package com.gumeng.usercenter.controller;

import cn.hutool.core.util.IdUtil;
import com.gumeng.usercenter.common.BaseResponse;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.common.ResultUtils;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.service.UserService;
import com.gumeng.usercenter.utils.QiniuUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.gumeng.usercenter.contant.CommonConstant.QINIU_DOMAIN;
import static com.gumeng.usercenter.contant.UserConstant.USER_DEFAULT_AVATAR;


/**
 * 文件上传和下载
 */
@RestController
@RequestMapping("/common")
@Api(tags = "头像上传接口")
@Slf4j
public class CommonController {


    @Resource
    UserService userService;

    /**
     * 图片上传
     * @param file
     * @return
     */
    @ApiOperation(value = "图片上传")
    @PostMapping("/upload")
    public BaseResponse<String> upload(MultipartFile file, HttpServletRequest request){

        User loginUser = userService.getLoginUser(request);
        String avatarUrl = loginUser.getAvatarUrl();

        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = IdUtil.simpleUUID() + suffix;

        try {
            // 将文件上传到七牛云服务器
            QiniuUtils.upload2Qiniu(file.getBytes(),fileName);
            // 删除原有头像
            if (StringUtils.isNotBlank(avatarUrl) && !USER_DEFAULT_AVATAR.equals(avatarUrl) && !avatarUrl.contains("@qq.com")){
                String oldFileName = StringUtils.substringAfterLast(avatarUrl,'/');
                QiniuUtils.deleteFileFromQiniu(oldFileName);
            }

            avatarUrl = QINIU_DOMAIN + fileName;
            loginUser.setAvatarUrl(avatarUrl);
            boolean updated = userService.updateById(loginUser);
            if (!updated){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"修改头像失败");
            }

            return ResultUtils.success(avatarUrl);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图片上传失败");
        }
    }


}
