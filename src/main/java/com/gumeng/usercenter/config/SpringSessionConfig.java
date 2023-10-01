package com.gumeng.usercenter.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import javax.servlet.ServletRequestListener;
import javax.websocket.server.ServerEndpointConfig;

@Configuration
public class SpringSessionConfig extends ServerEndpointConfig.Configurator implements ServletRequestListener {

    @Bean
    public CookieSerializer httpSessionIdResolver() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        // 取消仅限同一站点设置
        cookieSerializer.setSameSite(null);
        return cookieSerializer;
    }

}