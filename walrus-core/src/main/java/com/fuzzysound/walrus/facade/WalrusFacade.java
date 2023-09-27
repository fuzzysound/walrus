package com.fuzzysound.walrus.facade;

import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;

import java.math.BigInteger;

public interface WalrusFacade {
    void postDeposit(WalrusBlock block, WalrusTransaction transaction);

    void postWithdraw(WalrusBlock block, WalrusTransaction transaction, Long timestamp);

    void withdraw(String username, String password, String toAddress, BigInteger value);

    void updateBlockConfirmation(WalrusBlock witnessBlock, WalrusTransaction transaction, int blockConfirmationCount);
}
