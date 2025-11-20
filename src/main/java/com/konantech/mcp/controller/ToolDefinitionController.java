package com.konantech.mcp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.konantech.mcp.model.ToolDefinition;
import com.konantech.mcp.repository.ToolDefinitionRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolDefinitionController {

    private final ToolDefinitionRepository toolDefinitionRepository;

    @GetMapping
    public List<ToolDefinition> getAllEnabledTools() {
        return toolDefinitionRepository.selectAllEnabled();
    }

    @PostMapping
    public ResponseEntity<ToolDefinition> createTool(@RequestBody ToolDefinition toolDefinition) {
        if (toolDefinition.getEnabled() == null) {
            toolDefinition.setEnabled(true);
        }
        if (toolDefinition.getVersion() == null) {
            toolDefinition.setVersion(1);
        }
        toolDefinitionRepository.insertToolDefinition(toolDefinition);
        ToolDefinition created = toolDefinitionRepository.selectByName(toolDefinition.getName());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}

