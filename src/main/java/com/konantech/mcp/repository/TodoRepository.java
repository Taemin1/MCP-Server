package com.konantech.mcp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.konantech.mcp.entity.Todo;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {
	// 모든 정보 조회
	List<Todo> findAll();
	// 제목으로 할일 존재 여부 확인
    boolean existsByTitle(String title);
    // 최신 3개 할일 조회
    List<Todo> findTop3ByOrderByCreatedAtDesc();
	// 오늘 생성된 할일 상위 5개 조회
    List<Todo> findTop5ByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startOfDay, LocalDateTime endOfDay);
    // 할일 삭제
	void deleteById(UUID id);
	// ID로 할일 조회
	Optional<Todo> findById(UUID id);

}