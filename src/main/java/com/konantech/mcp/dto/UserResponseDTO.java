package com.konantech.mcp.dto;

import java.time.ZonedDateTime;

import com.konantech.mcp.entity.User;
import com.konantech.mcp.enums.Platform;
import com.konantech.mcp.enums.Role;

public record UserResponseDTO(String id, String username, Role role, Platform platform, ZonedDateTime createdAt) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(user.getId(), user.getUsername(), user.getRole(), user.getPlatform(), user.getCreatedAt());
    }
}
