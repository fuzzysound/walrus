package com.fuzzysound.walrus.wallet.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalrusWalletFileInfo {
    private final String filePath;
    private final String address;
}
