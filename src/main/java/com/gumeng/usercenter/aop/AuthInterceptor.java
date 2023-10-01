package com.gumeng.usercenter.aop;

import com.gumeng.usercenter.annotation.AuthCheck;
import com.gumeng.usercenter.common.ErrorCode;
import com.gumeng.usercenter.common.UserRole;
import com.gumeng.usercenter.exception.BusinessException;
import com.gumeng.usercenter.model.domain.User;
import com.gumeng.usercenter.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;


/**
 * 权限校验 AOP
 *
 * @author gumeng
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        List<UserRole> anyRole = Arrays.asList(authCheck.anyRole());
        UserRole mustRole = authCheck.mustRole();

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 当前登录用户
        User user = userService.getLoginUser(request);
        UserRole userRole = UserRole.values()[user.getUserRole()];

        // 拥有任意权限即通过
        if (!anyRole.isEmpty() && !anyRole.contains(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 必须有指定权限才通过
        if (mustRole != null && mustRole != userRole) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 通过权限校验，放行
        return joinPoint.proceed();
    }

}

