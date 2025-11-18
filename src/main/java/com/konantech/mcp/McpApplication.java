package com.konantech.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.konantech.mcp.service.TodoService;


@SpringBootApplication
public class McpApplication {
	public static void main(String[] args) {        
		SpringApplication.run(McpApplication.class, args);
	}

    @Bean
    public ToolCallbackProvider toolCallbackProvider(TodoService tools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build();
    }
}