package com.fuzzysound.walrus.task.infrastructure.repository;

import com.fuzzysound.walrus.task.infrastructure.entity.LastConfirmedBlockNumberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LastConfirmedBlockNumberRepository
    extends JpaRepository<LastConfirmedBlockNumberEntity, Long> {
    Optional<LastConfirmedBlockNumberEntity> findByDistKey(String distKey);
}
