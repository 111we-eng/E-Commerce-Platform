package com.ecommerce.config;

import com.ecommerce.interceptor.JwtAuthInterceptor;
import com.ecommerce.interceptor.RequestLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Autowired
    private RequestLogInterceptor requestLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 请求日志拦截器 — 拦截所有请求
        registry.addInterceptor(requestLogInterceptor)
                .addPathPatterns("/api/**");

        // JWT 认证拦截器 — 拦截需要认证的接口
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                // 放行：登录、注册、商品浏览（不需要登录也能看）
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/products/**"
                );
    }
}
