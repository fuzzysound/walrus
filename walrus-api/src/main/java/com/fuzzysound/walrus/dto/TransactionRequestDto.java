package com.fuzzysound.walrus.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class TransactionRequestDto {
    private final String to;
    private final String value;
}
