package com.fuzzysound.walrus.wallet.service;

import com.fuzzysound.walrus.wallet.model.WalrusWallet;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;

import java.math.BigInteger;
import java.util.List;

public interface WalletService {
    List<WalrusTransaction> filterTransactionsDepositToWalrus(List<WalrusTransaction> transactions);

    List<WalrusTransaction> filterTransactionWithdrawFromWalrus(List<WalrusTransaction> transactions);

    void processConfirmedTransaction(String transactionHash);

    WalrusWallet createWallet(String username, String password);

    WalrusWallet getWallet(String username, String password);

    void prepareWithdrawal(String transactionHash, String fromAddress, BigInteger value);

    void finishWithdrawal(String transactionHash);

    void undoWithdrawal(String transactionHash);

    boolean addressExists(String address);
}
