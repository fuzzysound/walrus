package com.fuzzysound.walrus.wallet.service;

import com.fuzzysound.walrus.wallet.model.WalrusWalletFileInfo;

public interface WalletFileService {
    WalrusWalletFileInfo createWalletFile(String username, String password);
}
