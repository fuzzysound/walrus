package com.fuzzysound.walrus.web3;

import com.fuzzysound.walrus.common.exception.Web3Exception;
import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.tx.Transfer;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class Web3Utils {
    public static WalrusBlock toWalrusBlock(EthBlock.Block originalBlock) {
        return WalrusBlock.builder()
                .blockHash(originalBlock.getHash())
                .blockNumber(originalBlock.getNumber())
                .timestamp(originalBlock.getTimestamp().longValue())
                .transactions(toWalrusTransactions(originalBlock.getTransactions()))
                .build();
    }

    public static WalrusTransaction toWalrusTransaction(Transaction originalTransaction) {
        return WalrusTransaction.builder()
                .transactionHash(originalTransaction.getHash())
                .from(originalTransaction.getFrom())
                .to(originalTransaction.getTo())
                .value(originalTransaction.getValue())
                .build();
    }

    public static List<WalrusTransaction> toWalrusTransactions(List<EthBlock.TransactionResult> originalTransactions) {
        return originalTransactions.stream()
                .map(originalTransaction -> (EthBlock.TransactionObject) originalTransaction)
                .map(Web3Utils::toWalrusTransaction)
                .toList();
    }

    public static byte[] getSignedTransaction(
            Web3j web3j,
            Credentials credentials,
            String toAddress,
            BigInteger value
    ) {
        try {
            WalrusRawTransactionManager rawTransactionManager = new WalrusRawTransactionManager(web3j, credentials);
            Transfer transfer = new Transfer(web3j, rawTransactionManager);
            return rawTransactionManager.getSignedTransaction(
                    transfer.requestCurrentGasPrice(),
                    Transfer.GAS_LIMIT,
                    toAddress,
                    "",
                    value
            );
        } catch (IOException e) {
            throw new Web3Exception(e);
        }
    }

    public static String getTransactionHash(byte[] signedTransaction) {
        return Numeric.toHexString(Hash.sha3(signedTransaction));
    }

    public static EthSendTransaction sendSignedTransaction(byte[] signedTransaction, Web3j web3j) {
        try {
            String hexValue = Numeric.toHexString(signedTransaction);
            return web3j.ethSendRawTransaction(hexValue).send();
        } catch (IOException e) {
            throw new Web3Exception(e);
        }
    }
}
