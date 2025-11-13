package com.konantech.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpApplication {

	public static void main(String[] args) {        
		// STDIO 모드 확인
        String transport = System.getProperty("spring.ai.mcp.server.transport");
        if (!"STDIO".equals(transport)) {
            System.err.println("Warning: STDIO transport not configured");
        }
		SpringApplication.run(McpApplication.class, args);
	}

}