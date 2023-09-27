package com.fuzzysound.walrus.facade;

import com.fuzzysound.walrus.common.TransactionHelper;
import com.fuzzysound.walrus.common.Web3Constants;
import com.fuzzysound.walrus.common.exception.TransactionFailedException;
import com.fuzzysound.walrus.event.model.EventHistory;
import com.fuzzysound.walrus.event.model.TransactionStatus;
import com.fuzzysound.walrus.event.service.EventHistoryService;
import com.fuzzysound.walrus.task.model.TransactionBlockConfirmationStatus;
import com.fuzzysound.walrus.task.service.TaskProgressService;
import com.fuzzysound.walrus.wallet.model.WalrusWallet;
import com.fuzzysound.walrus.wallet.service.WalletService;
import com.fuzzysound.walrus.web3.Web3Utils;
import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import com.fuzzysound.walrus.web3.service.Web3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DefaultWalrusFacade implements WalrusFacade {
    private final EventHistoryService eventHistoryService;
    private final TaskProgressService taskProgressService;
    private final WalletService walletService;
    private final Web3Service web3Service;

    @Override
    public void postDeposit(WalrusBlock block, WalrusTransaction transaction) {
        log.info("Post process deposit for transaction hash {}.", transaction.getTransactionHash());
        EventHistory eventHistory = EventHistory.builder()
                .transaction(transaction)
                .address(transaction.getTo())
                .transactionStatus(TransactionStatus.PENDING)
                .blockConfirmationCount(Web3Constants.INIT_BLOCK_CONFIRMATION_COUNT)
                .timestamp(block.getTimestamp())
                .build();
        eventHistoryService.addEventHistory(eventHistory);
        TransactionBlockConfirmationStatus transactionBlockConfirmationStatus =
                TransactionBlockConfirmationStatus.builder()
                        .transactionHash(transaction.getTransactionHash())
                        .lastConfirmedBlockNumber(block.getBlockNumber())
                        .currentBlockConfirmationCount(Web3Constants.INIT_BLOCK_CONFIRMATION_COUNT)
                        .isSettled(false)
                        .build();
        taskProgressService.upsertTransactionBlockConfirmationStatus(transactionBlockConfirmationStatus);
        log.info("Post process deposit done for transaction hash {}.", transaction.getTransactionHash());
    }

    @Override
    public void postWithdraw(WalrusBlock block, WalrusTransaction transaction, Long timestamp) {
        log.info("Post process withdraw for transaction hash {}.", transaction.getTransactionHash());
        walletService.finishWithdrawal(transaction.getTransactionHash());
        EventHistory eventHistory = EventHistory.builder()
                .transaction(transaction)
                .address(transaction.getFrom())
                .transactionStatus(TransactionStatus.PENDING)
                .blockConfirmationCount(Web3Constants.INIT_BLOCK_CONFIRMATION_COUNT)
                .timestamp(timestamp)
                .build();
        eventHistoryService.addEventHistory(eventHistory);
        TransactionBlockConfirmationStatus transactionBlockConfirmationStatus =
                TransactionBlockConfirmationStatus.builder()
                        .transactionHash(transaction.getTransactionHash())
                        .lastConfirmedBlockNumber(block.getBlockNumber())
                        .currentBlockConfirmationCount(Web3Constants.INIT_BLOCK_CONFIRMATION_COUNT)
                        .isSettled(false)
                        .build();
        taskProgressService.upsertTransactionBlockConfirmationStatus(transactionBlockConfirmationStatus);
        log.info("Post process withdrawal for transaction hash {}.", transaction.getTransactionHash());
    }

    @Override
    public void withdraw(String username, String password, String toAddress, BigInteger value) {
        WalrusWallet wallet = walletService.getWallet(username, password);
        String fromAddress = wallet.getAddress();
        log.info("Withdrawal request from {} to {}, {} wei.", fromAddress, toAddress, value);
        byte[] signedTransaction = web3Service.getSignedTransaction(wallet, toAddress, value);
        String transactionHash = web3Service.getTransactionHash(signedTransaction);
        walletService.prepareWithdrawal(transactionHash, fromAddress, value);
        long timestamp = Instant.now().getEpochSecond();
        web3Service.sendSignedTransaction(signedTransaction)
                .handle((blockAndTransaction, ex) -> {
                    if (ex == null) {
                        WalrusBlock block = blockAndTransaction.getFirst();
                        WalrusTransaction transaction = blockAndTransaction.getSecond();
                        log.info("Transaction successfully mined with transaction hash {} and block number {}.",
                                transaction.getTransactionHash(), block.getBlockNumber());
                        postWithdraw(block, transaction, timestamp);
                        log.info("Withdrawal post processing successfully done for transaction hash {}.",
                                transaction.getTransactionHash());
                    } else if (ex instanceof TransactionFailedException) {
                        log.error("Transaction failed for withdrawal request from {} to {}, {} wei, " +
                                "proceed to undo withdrawal...", fromAddress, toAddress, value, ex);
                        walletService.undoWithdrawal(transactionHash);
                        log.info("Undo of withdrawal from {} to {}, {} wei successfully done.",
                                fromAddress, toAddress, value);
                    } else {
                        log.warn("Transaction succeeded for withdrawal request from {} to {}, {} wei, " +
                                "but post processing failed. It will be complemented by task manager.",
                                fromAddress, toAddress, value, ex);
                    }
                    return 0;
                });
    }

    @Override
    public void updateBlockConfirmation(WalrusBlock witnessBlock, WalrusTransaction transaction, int blockConfirmationCount) {
        log.info("Updating block confirmation for transaction hash {}, block confirmation count {}.",
                transaction.getTransactionHash(), blockConfirmationCount);
        TransactionStatus transactionStatus = blockConfirmationCount == Web3Constants.MAX_BLOCK_CONFIRMATION_COUNT
                ? TransactionStatus.CONFIRMED : TransactionStatus.MINED;
        if (walletService.addressExists(transaction.getFrom())) {
            EventHistory fromAddressEventHistory = EventHistory.builder()
                    .transaction(transaction)
                    .address(transaction.getFrom())
                    .transactionStatus(transactionStatus)
                    .blockConfirmationCount(blockConfirmationCount)
                    .timestamp(witnessBlock.getTimestamp())
                    .build();
            eventHistoryService.addEventHistory(fromAddressEventHistory);
        }
        if (walletService.addressExists(transaction.getTo())) {
            EventHistory toAddressEventHistory = EventHistory.builder()
                    .transaction(transaction)
                    .address(transaction.getTo())
                    .transactionStatus(transactionStatus)
                    .blockConfirmationCount(blockConfirmationCount)
                    .timestamp(witnessBlock.getTimestamp())
                    .build();
            eventHistoryService.addEventHistory(toAddressEventHistory);
        }
        TransactionBlockConfirmationStatus transactionBlockConfirmationStatus
                = taskProgressService.getTransactionBlockConfirmationStatus(transaction.getTransactionHash());
        boolean isSettled = transactionBlockConfirmationStatus.isSettled();
        if (blockConfirmationCount == Web3Constants.MAX_BLOCK_CONFIRMATION_COUNT && !isSettled) {
            walletService.processConfirmedTransaction(transactionBlockConfirmationStatus.getTransactionHash());
            isSettled = true;
        }
        TransactionBlockConfirmationStatus newTransactionBlockConfirmationStatus =
                TransactionBlockConfirmationStatus.builder()
                        .transactionHash(transaction.getTransactionHash())
                        .lastConfirmedBlockNumber(witnessBlock.getBlockNumber())
                        .currentBlockConfirmationCount(blockConfirmationCount)
                        .isSettled(isSettled)
                        .build();
        taskProgressService.upsertTransactionBlockConfirmationStatus(newTransactionBlockConfirmationStatus);
        log.info("Updated block confirmation for transaction hash {}, block confirmation count {}.",
                transaction.getTransactionHash(), blockConfirmationCount);
        if (isSettled) {
            log.info("Transaction settled for transaction hash {}.", transaction.getTransactionHash());
        }
    }
}
