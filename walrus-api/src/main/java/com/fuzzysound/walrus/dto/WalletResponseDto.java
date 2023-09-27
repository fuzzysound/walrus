package com.fuzzysound.walrus.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
public class WalletResponseDto {
    private final String username;
    private final String address;
    private final String balance;
}
