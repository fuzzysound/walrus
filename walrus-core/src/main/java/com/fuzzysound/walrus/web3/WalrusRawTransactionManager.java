package com.fuzzysound.walrus.web3;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.service.TxSignService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;

public class WalrusRawTransactionManager extends RawTransactionManager {
    public WalrusRawTransactionManager(Web3j web3j, Credentials credentials, long chainId) {
        super(web3j, credentials, chainId);
    }

    public WalrusRawTransactionManager(Web3j web3j, TxSignService txSignService, long chainId) {
        super(web3j, txSignService, chainId);
    }

    public WalrusRawTransactionManager(Web3j web3j, Credentials credentials, long chainId, TransactionReceiptProcessor transactionReceiptProcessor) {
        super(web3j, credentials, chainId, transactionReceiptProcessor);
    }

    public WalrusRawTransactionManager(Web3j web3j, Credentials credentials, long chainId, int attempts, long sleepDuration) {
        super(web3j, credentials, chainId, attempts, sleepDuration);
    }

    public WalrusRawTransactionManager(Web3j web3j, Credentials credentials) {
        super(web3j, credentials);
    }

    public WalrusRawTransactionManager(Web3j web3j, Credentials credentials, int attempts, int sleepDuration) {
        super(web3j, credentials, attempts, sleepDuration);
    }

    public byte[] getSignedTransaction(
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            String data,
            BigInteger value
    ) throws IOException {
        BigInteger nonce = getNonce();
        RawTransaction rawTransaction =
                RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);
        String hexSignedTransaction = sign(rawTransaction);
        return Numeric.hexStringToByteArray(hexSignedTransaction);
    }
}
