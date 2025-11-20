package com.konantech.mcp.repository;

import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.konantech.mcp.entity.Todo;

@Mapper
public interface TodoRepository {

    List<Todo> findAll();

    Todo findById(@Param("id") UUID id);

    boolean existsByTitle(@Param("title") String title);

    void deleteById(@Param("id") UUID id);

    void insertTodo(Todo todo);

    void updateTodo(Todo todo);
}
