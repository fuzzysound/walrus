package com.fuzzysound.walrus.task.infrastructure.repository;

import com.fuzzysound.walrus.task.infrastructure.entity.TransactionBlockConfirmationStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionBlockConfirmationStatusRepository
    extends JpaRepository<TransactionBlockConfirmationStatusEntity, Long> {
    List<TransactionBlockConfirmationStatusEntity> findAllByCurrentBlockConfirmationCountLessThan(
            int currentBlockConfirmationCount
    );

    Optional<TransactionBlockConfirmationStatusEntity> findByTransactionHash(String transactionHash);
}
