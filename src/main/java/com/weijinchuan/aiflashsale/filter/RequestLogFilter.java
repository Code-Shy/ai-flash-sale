package com.weijinchuan.aiflashsale.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求日志过滤器
 *
 * 作用：
 * 1. 记录请求方法和路径
 * 2. 记录请求耗时
 * 3. 记录响应状态码
 */
@Slf4j
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        long startTime = System.currentTimeMillis();

        try {
            log.info("请求开始，requestId={}, method={}, uri={}, queryString={}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString());

            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - startTime;
            log.info("请求结束，requestId={}, status={}, costMs={}",
                    requestId,
                    response.getStatus(),
                    cost);
        }
    }
}