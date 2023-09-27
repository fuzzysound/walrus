package com.fuzzysound.walrus.wallet.infrastructure.converter;

import com.fuzzysound.walrus.wallet.infrastructure.entity.WalletEntity;
import com.fuzzysound.walrus.wallet.model.WalrusWallet;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class WalletEntityConverter {
    public WalletEntity toEntity(WalrusWallet model) {
        WalletEntity entity = new WalletEntity();
        entity.setUsername(model.getUsername());
        entity.setBase64Password(new String(Base64.encode(model.getPassword().getBytes())));
        entity.setFilePath(model.getFilePath());
        entity.setAddress(model.getAddress());
        entity.setBalance(model.getBalance().toString());
        return entity;
    }

    public WalrusWallet fromEntity(WalletEntity entity) {
        return WalrusWallet.builder()
                .username(entity.getUsername())
                .password(new String(Base64.decode(entity.getBase64Password().getBytes())))
                .filePath(entity.getFilePath())
                .address(entity.getAddress())
                .balance(new BigInteger(entity.getBalance()))
                .build();
    }
}
