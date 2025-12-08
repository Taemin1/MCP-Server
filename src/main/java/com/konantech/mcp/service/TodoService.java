package com.konantech.mcp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.konantech.mcp.domain.Todo;
import com.konantech.mcp.dto.TodoResponseDTO;
import com.konantech.mcp.mapper.TodoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoService {

    private static final Logger logger = LoggerFactory.getLogger(TodoService.class);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final TodoMapper todoMapper;
    private final UnsplashService unsplashService;

    // 외부 API 호출
    private final RestClient.Builder restClientBuilder;
    @Value("${externalApiServer.base-url}")
    private String baseUrl;

    // 공통 헤더 / 기본 URL 설정한 RestClient 생성
    private RestClient createClient() {
        return restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }
    
    // jwt token값 추출
    private String resolveBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken().getTokenValue();
        }
        if (authentication != null) {
            Object credentials = authentication.getCredentials();
            if (credentials instanceof Jwt jwt) {
                return jwt.getTokenValue();
            }
            if (credentials instanceof String token) {
                return token;
            }
        }
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String authHeader = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                return authHeader.substring(7);
            }
        }
        throw new IllegalStateException("JWT 토큰을 SecurityContext에서 찾을 수 없습니다.");
    }

    @Tool(
            name = "getAllTodos",
            description = "모든 할일 목록을 조회합니다. 반환값은 JSON 배열 문자열입니다."
    )
    public String getAllTodos(
            @ToolParam(description = "조회할 할일 구분")
            String id
    ) throws JsonProcessingException {
        logger.info("[TOOL] getAllTodos 실행");
        List<TodoResponseDTO> todos = todoMapper.findAll().stream()
                .map(TodoResponseDTO::from)
                .toList();
        return objectMapper.writeValueAsString(todos);
    }

    @Tool(
            name = "getTodoById",
            description = "특정 ID로 할일을 조회합니다."
    )
    public String getTodoById(
            @ToolParam(description = "조회할 대상의 ID (UUID 문자열)")
            String id) throws JsonProcessingException {
        logger.info("[TOOL] getTodoById 실행 : {}", id);
        UUID uuid = UUID.fromString(id);
        Todo todo = todoMapper.findById(uuid);
        if (todo == null) {
            throw new IllegalArgumentException("Todo not found for id: " + id);
        }
        return objectMapper.writeValueAsString(TodoResponseDTO.from(todo));
    }

    @Tool(
            name = "removeTodoById",
            description = "특정 ID 기준으로 할일을 삭제합니다."
    )
    @Transactional
    public void removeTodoById(
            @ToolParam(description = "삭제할 대상의 ID (UUID 문자열)")
            String id) {
        logger.info("[TOOL] removeTodoById 실행 : {}", id);
        UUID uuid = UUID.fromString(id);
        todoMapper.deleteById(uuid);
    }

    @Tool(
            name = "createTodo",
            description = "새 할일을 등록합니다. title, description, priority, isDone 파라미터를 받습니다."
    )
    @Transactional
    public TodoResponseDTO createTodo(
            @ToolParam(description = "작성할 할일의 제목") String title,
            @ToolParam(description = "작성할 할일의 설명") String description,
            @ToolParam(description = "우선순위 (1=낮음, 2=중간, 3=높음)") Integer priority,
            @ToolParam(description = "완료 여부") Boolean isDone) {
        logger.info("[TOOL] createTodo 실행");
        Todo entity = new Todo();
        entity.setId(UUID.randomUUID());
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setPriority(priority);
        entity.setIsDone(isDone);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        todoMapper.insertTodo(entity);
        return TodoResponseDTO.from(entity);
    }

    @Tool(
            name = "updateTodo",
            description = "기존 할일 정보를 수정합니다. id(필수)와 수정할 필드를 모두 받습니다."
    )
    @Transactional
    public String updateTodo(
            @ToolParam(description = "수정할 대상의 ID (UUID 문자열)") String id,
            @ToolParam(description = "수정 후 제목") String title,
            @ToolParam(description = "수정 후 설명") String description,
            @ToolParam(description = "우선순위 (1=낮음, 2=중간, 3=높음)") Integer priority,
            @ToolParam(description = "완료 여부") Boolean isDone) {
        logger.info("[TOOL] updateTodo 실행");
        UUID uuid = UUID.fromString(id);
        Todo todo = todoMapper.findById(uuid);
        if (todo == null) {
            throw new IllegalArgumentException("Todo not found for id: " + uuid);
        }
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority);
        todo.setIsDone(isDone);
        todo.setUpdatedAt(LocalDateTime.now());
        todoMapper.updateTodo(todo);
        return "할일 정보가 성공적으로 수정되었습니다.";
    }

    @Tool(
            name = "getTodoByTitle",
            description = "할일 제목(필수)으로 할일 목록을 조회합니다. 반환값은 JSON 배열 문자열입니다."
    )
    public String getTodoByTitle(
            @ToolParam(description = "조회할 할일의 이름입니다. 문자열 형태입니다.")
            String title
    ) throws JsonProcessingException {
        logger.info("[TOOL] getTodoByTitle 실행");
        List<TodoResponseDTO> todos = todoMapper.findByTitle(title).stream()
                .map(TodoResponseDTO::from)
                .toList();
        return objectMapper.writeValueAsString(todos);
    }

    @Tool(
            name = "getImageByKeyword",
            description = "키워드로 이미지를 검색하여 바이너리 데이터를 반환합니다. Unsplash API를 사용합니다."
    )
    public byte[] getImageByKeyword(
            @ToolParam(description = "검색할 키워드 (예: 강아지, 블로그 로봇)") String keyword
    ) {
        logger.info("[TOOL] getImageByKeyword 실행 : {}", keyword);
        return unsplashService.fetchImageByKeyword(keyword);
    }

    @Tool(
            name = "getImageUrlByKeyword",
            description = "키워드로 이미지를 검색해 첫 번째 결과의 이미지 URL을 반환합니다. Unsplash API를 사용합니다."
    )
    public String getImageUrlByKeyword(
            @ToolParam(description = "검색할 키워드 (예: 강아지, 블로그 로봇)") String keyword
    ) {
        logger.info("[External API TOOL] getImageUrlByKeyword 실행 : {}", keyword);
        return unsplashService.fetchImageUrlByKeyword(keyword);
    }

    @Tool(
        name = "findClothingSale",
        description = "세일 중인 의류 상품 목록을 조회합니다. 반환 값은 ClothingSale 리스트이며, 데이터가 없으면 빈 리스트를 반환합니다."
    )
    public Object findClothingSale(
            @ToolParam(description = "조회할 의류 상품 세일 상태 구분.(true/false) / true : 세일중인 상품을 조회합니다. / false : 세일 상태가 아닌 상품을 조회합니다. (필수)")
            String isOnSale
    ) {
        logger.info("[External API TOOL : include jwt] findClothingSale 실행");

        RestClient client = createClient();
        try {
            if (isOnSale == null) {
                throw new IllegalArgumentException("isOnSale 파라미터는 true/false 중 하나로 전달되어야 합니다.");
            }
            String bearer = resolveBearerToken();
            JsonNode response = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/cloth/find/onSale")
                            .queryParam("isOnSale", isOnSale)
                            .build()
                    )
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
            // logger.info("[External API TOOL : include jwt] response : {}", response);

            if (response == null || !response.isArray() || response.isEmpty()) {
                throw new IllegalStateException("검색 결과가 없습니다: " );
            }

            return response;
        } catch (RestClientException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }
}
