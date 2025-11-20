// java
  package com.konantech.mcp.service;

  import java.time.LocalDateTime;
  import java.util.List;
  import java.util.UUID;

  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.ai.tool.annotation.Tool;
  import org.springframework.ai.tool.annotation.ToolParam;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;

  import com.fasterxml.jackson.core.JsonProcessingException;
  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.fasterxml.jackson.databind.SerializationFeature;
  import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

      @Tool(
          name = "getAllTodos",
          description = "모든 할일 목록을 조회합니다. 반환값은 JSON 배열 문자열입니다."
      )
      public String getAllTodos() throws JsonProcessingException {
          logger.info("[TOOL] getAllTodos 실행");
          List<TodoResponseDTO> todos = todoRepository.findAll().stream()
                  .map(TodoResponseDTO::from)
                  .toList();
          return objectMapper.writeValueAsString(todos);
      }

      @Tool(
          name = "getTodoById",
          description = "할일 ID로 특정 할일 정보를 조회합니다."
      )
      public String getTodoById(
              @ToolParam(description = "조회할 할일 ID (UUID 문자열)")
              String id) throws JsonProcessingException {
          logger.info("[TOOL] getTodoById 실행 : {}", id);
          UUID uuid = UUID.fromString(id);
          Todo todo = todoRepository.findById(uuid);
          if (todo == null) {
              throw new IllegalArgumentException("Todo not found for id: " + id);
          }
          return objectMapper.writeValueAsString(TodoResponseDTO.from(todo));
      }

      @Tool(
          name = "removeTodoById",
          description = "할일 ID 기준으로 삭제합니다."
      )
      @Transactional
      public void removeTodoById(
              @ToolParam(description = "삭제할 할일 ID (UUID 문자열)")
              String id) {
          logger.info("[TOOL] removeTodoById 실행 : {}", id);
          UUID uuid = UUID.fromString(id);
          todoRepository.deleteById(uuid);
      }

      @Tool(
          name = "createTodo",
          description = "새 할일을 등록합니다. title, description, priority, isDone을 개별 파라미터로 받습니다."
      )
      @Transactional
      public TodoResponseDTO createTodo(
              @ToolParam(description = "생성할 할일 제목") String title,
              @ToolParam(description = "생성할 할일 설명") String description,
              @ToolParam(description = "우선순위 (1=높음, 2=중간, 3=낮음)") Integer priority,
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
          todoRepository.insertTodo(entity);
          return TodoResponseDTO.from(entity);
      }

      @Tool(
          name = "updateTodo",
          description = "할일 정보를 수정합니다. id(필수)와 수정할 필드를 개별 파라미터로 받습니다."
      )
      @Transactional
      public String updateTodo(
              @ToolParam(description = "수정할 할일 ID (UUID 문자열)") String id,
              @ToolParam(description = "수정할 할일 제목") String title,
              @ToolParam(description = "수정할 할일 설명") String description,
              @ToolParam(description = "우선순위 (1=높음, 2=중간, 3=낮음)") Integer priority,
              @ToolParam(description = "완료 여부") Boolean isDone) {
          logger.info("[TOOL] updateTodo 실행");
          UUID uuid = UUID.fromString(id);
          Todo todo = todoRepository.findById(uuid);
          if (todo == null) {
              throw new IllegalArgumentException("Todo not found for id: " + uuid);
          }
          todo.setTitle(title);
          todo.setDescription(description);
          todo.setPriority(priority);
          todo.setIsDone(isDone);
          todo.setUpdatedAt(LocalDateTime.now());
          todoRepository.updateTodo(todo);
          return "할일 정보가 성공적으로 수정되었습니다.";
      }
  }