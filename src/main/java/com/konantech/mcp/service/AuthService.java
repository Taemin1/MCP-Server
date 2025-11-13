package com.konantech.mcp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.konantech.mcp.dto.UserRequestDTO;
import com.konantech.mcp.dto.UserResponseDTO;
import com.konantech.mcp.entity.User;
import com.konantech.mcp.enums.Platform;
import com.konantech.mcp.repository.UserRepository;

import java.util.List;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    
    @Tool(name = "MySecondToolWorks", description = "첫 번째 도구가 제대로 작동하는지 확인하는 도구입니다. MCP 서버 연결 확인이 필요할 때 이 도구를 사용하세요. 예: '첫 번째 도구가 작동하는지 보여줘', 'MCP 서버 연결 상태 확인해줘', '도구 테스트해줘'")
    public void MyFirstToolWorks() {
    	System.out.println("My Second Tool Works!!!!!!");
    }    	


    
}
