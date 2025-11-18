// java
package com.konantech.mcp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.konantech.mcp.dto.TodoRequestDTO;
import com.konantech.mcp.dto.TodoResponseDTO;
import com.konantech.mcp.entity.Todo;
import com.konantech.mcp.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {
    private static final Logger logger = LoggerFactory.getLogger(TodoService.class);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    private final TodoRepository todoRepository;

    @Tool(name = "getAllTodos", description = "모든 할일 정보를 조회합니다. 전체 할일 목록이 필요할 때 사용하세요.")
    public String getAllTodos() throws JsonProcessingException {
        logger.info("[TOOL] getAllTodos 실행");
        List<TodoResponseDTO> todos = todoRepository.findAll().stream()
                .map(TodoResponseDTO::from)
                .toList();
        return objectMapper.writeValueAsString(todos);
    }

    @Tool(name = "getPaginatedTodos", description = "페이지네이션과 정렬 옵션을 적용하여 할일 목록을 조회합니다.")
    public String getPaginatedTodos(
            @ToolParam(description = "조회할 페이지 번호(0부터 시작)")
            @RequestParam(defaultValue = "0")
            int pageNo,
            @ToolParam(description = "한 페이지에 표시할 할일 수")
            @RequestParam(defaultValue = "10")
            int pageSize,
            @ToolParam(description = "정렬할 속성 이름. Todo 엔티티 필드 사용 가능: 'title', 'priority', 'createdAt', 'updatedAt'")
            @RequestParam(defaultValue = "createdAt")
            String properties,
            @ToolParam(description = "정렬 방향. ASC 또는 DESC")
            @RequestParam(defaultValue = "DESC")
            Sort.Direction direction) throws JsonProcessingException {
        logger.info("[TOOL] getPaginatedTodos 실행");
        Sort sortType = Sort.by(direction, properties);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortType);

        List<TodoResponseDTO> todos = todoRepository.findAll(pageable).stream()
                .map(TodoResponseDTO::from)
                .toList();
        return objectMapper.writeValueAsString(todos);
    }

    @Tool(name = "getTodoById", description = "할일 ID로 특정 할일 정보를 조회합니다.")
    public String getTodoById(
            @ToolParam(description = "조회할 할일 ID")
            String id) throws JsonProcessingException {
        logger.info("[TOOL] getTodoById 실행 : " + id);
        UUID uuid = UUID.fromString(id);
        Todo todo = todoRepository.findById(uuid)
                .orElseThrow();
        return objectMapper.writeValueAsString(TodoResponseDTO.from(todo));
    }

    @Tool(name = "removeTodoById", description = "할일 ID 기준으로 삭제합니다.")
    @Transactional
    public void removeTodoById(
            @ToolParam(description = "삭제할 할일 ID")
            String id) {
        logger.info("[TOOL] removeTodoById 실행");
        UUID uuid = UUID.fromString(id);
        todoRepository.deleteById(uuid);
    }

    @Tool(name = "createTodo", description = "새로운 할일을 등록합니다. 파라미터는 TodoRequestDTO 형태의 JSON 문자열입니다.")
    @Transactional
    public TodoResponseDTO createTodo(
            @ToolParam(description = "생성할 할일 정보 JSON. title, description, priority, isDone")
            String todoRequestJson) {
        logger.info("[TOOL] createTodo 실행");
        try {
            TodoRequestDTO todoRequestDTO = objectMapper.readValue(todoRequestJson, TodoRequestDTO.class);
            Todo entity = new Todo();
            entity.setTitle(todoRequestDTO.getTitle());
            entity.setDescription(todoRequestDTO.getDescription());
            entity.setPriority(todoRequestDTO.getPriority());
            entity.setIsDone(todoRequestDTO.getIsDone());
            return TodoResponseDTO.from(todoRepository.save(entity));
        } catch (JsonProcessingException e) {
            logger.error("[TOOL] createTodo - invalid JSON payload", e);
            throw new IllegalArgumentException("Invalid Todo JSON payload", e);
        }
    }

    @Tool(name = "updateTodo", description = "할일 정보를 수정합니다. 파라미터는 TodoRequestDTO 형태의 JSON 문자열(id 필수).")
    @Transactional
    public void updateTodo(
            @ToolParam(description = "수정할 할일 정보 JSON. id(필수), title, description, priority, isDone")
            String todoRequestJson) {
        logger.info("[TOOL] updateTodo 실행");
        try {
            TodoRequestDTO todoRequestDTO = objectMapper.readValue(todoRequestJson, TodoRequestDTO.class);
            UUID uuid = todoRequestDTO.getId();
            Todo todo = todoRepository.findById(uuid)
                    .orElseThrow();
            todo.setTitle(todoRequestDTO.getTitle());
            todo.setDescription(todoRequestDTO.getDescription());
            todo.setPriority(todoRequestDTO.getPriority());
            todo.setIsDone(todoRequestDTO.getIsDone());
            todoRepository.save(todo);
        } catch (JsonProcessingException e) {
            logger.error("[TOOL] updateTodo - invalid JSON payload", e);
            throw new IllegalArgumentException("Invalid Todo JSON payload", e);
        }
    }

    @Tool(name = "checkTodoExists", description = "특정 제목의 할일이 존재하는지 확인합니다.")
    public boolean checkTodoExists(
            @ToolParam(description = "확인할 할일 제목")
            String title) {
        logger.info("[TOOL] checkTodoExists 실행");
        return todoRepository.existsByTitle(title);
    }

    @Tool(name = "getTop3RecentTodos", description = "최근에 생성된 할일 3개를 조회합니다.")
    public String getTop3RecentTodos() throws JsonProcessingException {
        logger.info("[TOOL] getTop3RecentTodos 실행");
        List<TodoResponseDTO> todos = todoRepository.findTop3ByOrderByCreatedAtDesc().stream()
                .map(TodoResponseDTO::from)
                .toList();
        return objectMapper.writeValueAsString(todos);
    }
    
    
    @Tool(name = "get5TodosToday", description = "오늘 할일 5개를 조회합니다.")
    public String get5TodosToday() throws JsonProcessingException {
        logger.info("[TOOL] get5TodosToday 실행");
        
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        
        List<TodoResponseDTO> todos = todoRepository
                .findTop5ByCreatedAtBetweenOrderByCreatedAtDesc(start, end)
                .stream()
                .map(TodoResponseDTO::from)
                .toList();
        return objectMapper.writeValueAsString(todos);
    }
}
