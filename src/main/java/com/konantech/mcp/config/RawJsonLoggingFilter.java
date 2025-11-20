package com.konantech.mcp.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RawJsonLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(wrappedRequest, wrappedResponse);

        // 요청 로깅
        byte[] requestBuf = wrappedRequest.getContentAsByteArray();
        if (requestBuf.length > 0 && wrappedRequest.getContentType() != null
            && wrappedRequest.getContentType().contains("application/json")) {
            String requestBody = new String(requestBuf, 0, requestBuf.length, StandardCharsets.UTF_8);
            System.out.println("RAW JSON REQUEST => " + requestBody);
        }

        // 응답 로깅
        /*
        byte[] responseBuf = wrappedResponse.getContentAsByteArray();
        if (responseBuf.length > 0) {
            String responseBody = new String(responseBuf, 0, responseBuf.length, StandardCharsets.UTF_8);
            System.out.println("RAW JSON RESPONSE => " + responseBody);
        }
        */
        
        
        wrappedResponse.copyBodyToResponse();
       
    }
}