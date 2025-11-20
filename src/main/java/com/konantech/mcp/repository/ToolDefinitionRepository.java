package com.konantech.mcp.repository;

  import org.apache.ibatis.annotations.Mapper;
  import org.apache.ibatis.annotations.Param;

import com.konantech.mcp.model.ToolDefinition;

import java.util.List;

  @Mapper
  public interface ToolDefinitionRepository {

      ToolDefinition selectByName(@Param("name") String name);

      List<ToolDefinition> selectAllEnabled();

      int insertToolDefinition(ToolDefinition toolDefinition);

      int updateToolDefinition(ToolDefinition toolDefinition);
  }