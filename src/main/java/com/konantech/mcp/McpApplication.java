package com.konantech.mcp;

import com.konantech.mcp.service.MechanicService;
import com.konantech.mcp.service.NaverNewsService;
import com.konantech.mcp.service.NewService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider tools(MechanicService mechanicService,
									  NewService newService,
									  NaverNewsService naverNewsService) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(mechanicService, newService, naverNewsService)
				.build();
	}
}

