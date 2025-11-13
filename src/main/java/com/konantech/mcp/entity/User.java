package com.konantech.mcp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import com.konantech.mcp.enums.Platform;
import com.konantech.mcp.enums.Role;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity // JPA
@Table(name = "users")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @CreationTimestamp
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);
}
