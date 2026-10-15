package com.example.ganpativargani.repository;

import com.example.ganpativargani.entity.Broadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BroadcastRepository extends JpaRepository<Broadcast, Long> {

    Optional<Broadcast> findByIdAndDeletedFalse(Long id);

    Page<Broadcast> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
}
