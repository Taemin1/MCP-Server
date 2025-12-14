package com.konantech.mcp.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class TokenProvider {

    /**
     * 현재 요청의 Authorization 헤더 전체를 반환합니다.
     * (Bearer 포함)
     */
    public String getAuthorizationHeader() {
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        return request.getHeader("Authorization");
    }
}
