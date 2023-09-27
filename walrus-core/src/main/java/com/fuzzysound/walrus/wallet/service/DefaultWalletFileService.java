package com.fuzzysound.walrus.wallet.service;

import com.fuzzysound.walrus.common.exception.WalletFileException;
import com.fuzzysound.walrus.wallet.model.WalrusWalletFileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;

@Service
public class DefaultWalletFileService implements WalletFileService {
    @Value("${walrus.wallet.dir}") private String walletDirPath;
    @Override
    public WalrusWalletFileInfo createWalletFile(String username, String password) {
        try {
            File walletDir = new File(walletDirPath);
            Bip39Wallet originalWallet = WalletUtils.generateBip39Wallet(password, walletDir);
            File walletFile = new File(walletDir, originalWallet.getFilename());
            String filePath = walletFile.getPath();
            Credentials credentials = WalletUtils.loadCredentials(password, filePath);
            return WalrusWalletFileInfo.builder()
                    .filePath(filePath)
                    .address(credentials.getAddress())
                    .build();
        } catch (CipherException | IOException e) {
            throw new WalletFileException("Failed to create wallet file.", e);
        }
    }
}
