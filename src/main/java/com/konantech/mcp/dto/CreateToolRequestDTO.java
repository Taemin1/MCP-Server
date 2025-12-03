package com.konantech.mcp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateToolRequestDTO {
    // tool name
    private String name;
    // tool description
    private String description;
    // api endpoint
    private String endpointUrl;
    // api method : GET, POST, UPDATE, DELETE
    private String httpMethod;

    private List<ParamDefinition> params;

    @Getter
    @Setter
    public static class ParamDefinition {
        private String name;
        private String type;
        private String description;
        private boolean required;
    }
}
