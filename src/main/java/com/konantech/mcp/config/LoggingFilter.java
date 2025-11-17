package com.konantech.mcp.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        String queryString = httpRequest.getQueryString();

        logger.info("========================================");
        logger.info("[REQUEST] {} {} {}", method, uri, queryString != null ? "?" + queryString : "");
        logger.info("[HEADERS] Content-Type: {}", httpRequest.getHeader("Content-Type"));

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("[RESPONSE] Status: {} | Duration: {}ms", httpResponse.getStatus(), duration);
            logger.info("========================================");
        }
    }
}
