package com.fuzzysound.walrus.wallet.infrastructure.repository;

import com.fuzzysound.walrus.wallet.infrastructure.entity.PendingWithdrawalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingWithdrawalRepository extends JpaRepository<PendingWithdrawalEntity, Long> {
    Optional<PendingWithdrawalEntity> findByTransactionHash(String transactionHash);

    List<PendingWithdrawalEntity> findAllByAddress(String address);
}
