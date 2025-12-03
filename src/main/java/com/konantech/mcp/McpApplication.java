package com.konantech.mcp;

import com.konantech.mcp.service.TodoService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;


@SpringBootApplication
public class McpApplication {
    private final Logger logger = Logger.getLogger(McpApplication.class.getName());
    private final CountDownLatch latch = new CountDownLatch(1);

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