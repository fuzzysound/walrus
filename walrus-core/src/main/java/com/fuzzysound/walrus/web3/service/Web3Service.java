package com.fuzzysound.walrus.web3.service;

import com.fuzzysound.walrus.wallet.model.WalrusWallet;
import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import org.springframework.data.util.Pair;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

public interface Web3Service {
    BigInteger getLastBlockNumber();

    WalrusBlock getBlockByNumber(BigInteger blockNumber);

    WalrusTransaction getTransactionByHash(String transactionHash);

    byte[] getSignedTransaction(WalrusWallet wallet, String toAddress, BigInteger value);

    String getTransactionHash(byte[] signedTransaction);

    CompletableFuture<Pair<WalrusBlock, WalrusTransaction>> sendSignedTransaction(byte[] signedTransaction);

    BigInteger getTransactionCost();
}
