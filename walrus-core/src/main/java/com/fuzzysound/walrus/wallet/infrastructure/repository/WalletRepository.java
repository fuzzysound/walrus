package com.fuzzysound.walrus.wallet.infrastructure.repository;

import com.fuzzysound.walrus.wallet.infrastructure.entity.WalletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
    List<WalletEntity> findAllByAddressIn(List<String> addressList);

    Optional<WalletEntity> findByAddress(String address);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WalletEntity w where w.address = :address")
    Optional<WalletEntity> findByAddressForUpdate(@Param("address") String address);

    Optional<WalletEntity> findByUsername(String username);
}
