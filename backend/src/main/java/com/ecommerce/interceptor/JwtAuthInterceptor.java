package com.ecommerce.interceptor;

import com.ecommerce.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 认证拦截器
 *
 * 原理说明：
 * 1. 从请求头 Authorization 中提取 Bearer Token
 * 2. 调用 JwtUtil 解析 Token，验证签名和有效期
 * 3. 验证通过 → 将 userId 和 username 存入 request 属性，供后续 Controller 使用
 * 4. 验证失败 → 返回 401 状态码，前端 Axios 响应拦截器自动跳转登录页
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String authHeader = request.getHeader("Authorization");

        // 没有 Authorization 头
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token缺失\"}");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            // 解析 Token → 验证签名 + 有效期
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            // 存入 request 属性，后续 Controller 通过 @RequestAttribute 获取
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);

            return true;

        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token已过期或无效，请重新登录\"}");
            return false;
        }
    }
}
