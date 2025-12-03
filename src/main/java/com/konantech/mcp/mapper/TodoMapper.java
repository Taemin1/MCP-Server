package com.konantech.mcp.mapper;

import com.konantech.mcp.domain.ClothingSale;
import com.konantech.mcp.domain.Todo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface TodoMapper {

    List<Todo> findAll();

    Todo findById(@Param("id") UUID id);

    boolean existsByTitle(@Param("title") String title);

    void deleteById(@Param("id") UUID id);

    void insertTodo(Todo todo);

    void updateTodo(Todo todo);

    List<ClothingSale> findAllClothingSale();
}
