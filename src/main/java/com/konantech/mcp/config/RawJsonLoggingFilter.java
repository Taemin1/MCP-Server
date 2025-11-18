package com.konantech.mcp.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RawJsonLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        filterChain.doFilter(wrappedRequest, response);

        byte[] body = wrappedRequest.getContentAsByteArray();
        if (body.length > 0) {
            String rawJson = new String(body, StandardCharsets.UTF_8);
            System.out.println("==== RAW JSON BODY ====");
            System.out.println(rawJson);
        }
    }
}


