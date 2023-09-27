package com.fuzzysound.walrus.controller;

import com.fuzzysound.walrus.ValidationException;
import com.fuzzysound.walrus.converter.DtoConverter;
import com.fuzzysound.walrus.dto.WalletResponseDto;
import com.fuzzysound.walrus.provider.AuthenticationProvider;
import com.fuzzysound.walrus.wallet.model.WalrusWallet;
import com.fuzzysound.walrus.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final DtoConverter dtoConverter;
    private final AuthenticationProvider authenticationProvider;

    private static final int USERNAME_MAX_LENGTH = 30;
    private static final int PASSWORD_MAX_LENGTH = 191;

    @PostMapping("wallet")
    public WalletResponseDto createWallet() {
        String username = authenticationProvider.getUsername();
        String password = authenticationProvider.getPassword();
        validate(username, password);
        WalrusWallet wallet = walletService.createWallet(username, password);
        return dtoConverter.toWalletResponseDto(wallet);
    }

    @GetMapping("wallet")
    public WalletResponseDto getWallet() {
        String username = authenticationProvider.getUsername();
        String password = authenticationProvider.getPassword();
        WalrusWallet wallet = walletService.getWallet(username, password);
        return dtoConverter.toWalletResponseDto(wallet);
    }

    private void validate(String username, String password) {
        if (username.length() > USERNAME_MAX_LENGTH) {
            throw new ValidationException("Username length must be less than or equal to " + USERNAME_MAX_LENGTH);
        }
        if (password.length() > PASSWORD_MAX_LENGTH) {
            throw new ValidationException("Password length must be less than or equal to " + PASSWORD_MAX_LENGTH);
        }
    }
}
