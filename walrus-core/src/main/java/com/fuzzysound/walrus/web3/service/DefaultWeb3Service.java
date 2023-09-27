package com.fuzzysound.walrus.web3.service;

import com.fuzzysound.walrus.common.exception.TransactionFailedException;
import com.fuzzysound.walrus.common.exception.Web3Exception;
import com.fuzzysound.walrus.wallet.model.WalrusWallet;
import com.fuzzysound.walrus.web3.Web3Utils;
import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class DefaultWeb3Service implements Web3Service {
    private final Web3j web3j;

    public DefaultWeb3Service(@Value("${walrus.node.endpoint}") String nodeEndpoint) {
        this.web3j = Web3j.build(new HttpService(nodeEndpoint));
    }



    @Override
    public BigInteger getLastBlockNumber() {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            throw new Web3Exception(e);
        }
    }

    @Override
    public WalrusBlock getBlockByNumber(BigInteger blockNumber) {
        try {
            EthBlock.Block originalBlock = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(blockNumber), true
            ).send().getBlock();
            return Web3Utils.toWalrusBlock(originalBlock);
        } catch (IOException e) {
            throw new Web3Exception(e);
        }
    }

    @Override
    public WalrusTransaction getTransactionByHash(String transactionHash) {
        try {
            Optional<Transaction> transactionOptional = web3j.ethGetTransactionByHash(transactionHash)
                    .send().getTransaction();
            if (transactionOptional.isPresent()) {
                return Web3Utils.toWalrusTransaction(transactionOptional.get());
            } else {
                throw new IllegalArgumentException(
                        String.format("Transaction of hash %s does not exist.", transactionHash)
                );
            }
        } catch (IOException e) {
            throw new Web3Exception(e);
        }
    }

    @Override
    public byte[] getSignedTransaction(WalrusWallet wallet, String toAddress, BigInteger value) {
        try {
            Credentials credentials = WalletUtils.loadCredentials(wallet.getPassword(), wallet.getFilePath());
            return Web3Utils.getSignedTransaction(web3j, credentials, toAddress, value);
        } catch (IOException | CipherException e) {
            throw new Web3Exception(e);
        }
    }

    @Override
    public String getTransactionHash(byte[] signedTransaction) {
        return Web3Utils.getTransactionHash(signedTransaction);
    }

    @Override
    public CompletableFuture<Pair<WalrusBlock, WalrusTransaction>> sendSignedTransaction(byte[] signedTransaction) {
        return CompletableFuture.supplyAsync(() -> Web3Utils.sendSignedTransaction(signedTransaction, web3j))
                .handle((ethSendTransaction, ex) -> {
                    if (ex != null) {
                        throw new TransactionFailedException(ex);
                    }
                    String transactionHash = ethSendTransaction.getTransactionHash();
                    try {
                        Optional<Transaction> transactionOptional = web3j.ethGetTransactionByHash(transactionHash)
                                .send().getTransaction();
                        if (transactionOptional.isPresent()) {
                            Transaction originalTransaction = transactionOptional.get();
                            WalrusBlock block = getBlockByNumber(originalTransaction.getBlockNumber());
                            WalrusTransaction transaction = Web3Utils.toWalrusTransaction(originalTransaction);
                            return Pair.of(block, transaction);
                        } else {
                            throw new Web3Exception("Transaction mined but lost, transaction hash: " + transactionHash);
                        }
                    } catch (IOException e) {
                        throw new Web3Exception(e);
                    }
                });
    }

    @Override
    public BigInteger getTransactionCost() {
        try {
            EthGasPrice gasPrice = web3j.ethGasPrice().send();
            return gasPrice.getGasPrice().multiply(Transfer.GAS_LIMIT);
        } catch (IOException e) {
            throw new Web3Exception(e);
        }
    }
}
