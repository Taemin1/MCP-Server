package com.konantech.mcp.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolDefinition {

      private Long id;
      private String name;
      private String displayName;
      private String description;

      private String toolType;
      private String language;
      private String definition;

      private String parameterSchema;

      private Integer timeoutMs;
      private Boolean enabled;
      private Integer version;

      private java.time.OffsetDateTime createdAt;
      private java.time.OffsetDateTime updatedAt;

  }