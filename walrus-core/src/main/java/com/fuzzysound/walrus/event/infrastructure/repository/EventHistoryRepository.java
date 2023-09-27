package com.fuzzysound.walrus.event.infrastructure.repository;

import com.fuzzysound.walrus.event.infrastructure.entity.EventHistoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventHistoryRepository extends JpaRepository<EventHistoryEntity, Long> {
    Optional<EventHistoryEntity> findByTransactionHashAndAddressAndBlockConfirmationCount(
            String transactionHash, String address, int blockConfirmationCount);

    List<EventHistoryEntity> findAllByAddressAndTimestampAfterAndTimestampBeforeOrderByTimestampDesc(
            String address, long fromTimestamp, long toTimestamp, Pageable pageable
    );

    List<EventHistoryEntity> findAllByAddressOrderByTimestampDesc(String address, Pageable pageable);
}
