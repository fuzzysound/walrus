package com.fuzzysound.walrus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fuzzysound.walrus.task.infrastructure.entity.TaskEntity;
import com.fuzzysound.walrus.task.infrastructure.entity.TransactionBlockConfirmationStatusEntity;
import com.fuzzysound.walrus.task.infrastructure.repository.TaskRepository;
import com.fuzzysound.walrus.task.infrastructure.repository.TransactionBlockConfirmationStatusRepository;
import com.fuzzysound.walrus.task.model.TaskStatus;
import com.fuzzysound.walrus.task.model.taskSpec.BlockConfirmationTrackingTaskSpec;
import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.fuzzysound.walrus.common.TestUtils.getRandomHexString;
import static com.fuzzysound.walrus.common.Web3Constants.MAX_BLOCK_CONFIRMATION_COUNT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class BlockConfirmationTrackingTaskManagerTest extends TaskManagerTest {
    @Autowired private BlockConfirmationTrackingTaskManager blockConfirmationTrackingTaskManager;
    @Autowired private TransactionBlockConfirmationStatusRepository transactionBlockConfirmationStatusRepository;
    @Autowired private TaskRepository taskRepository;
    @MockBean private DepositTrackingTaskManager depositTrackingTaskManager;
    @MockBean private WithdrawalTrackingTaskManager withdrawalTrackingTaskManager;
    private static final BigInteger LAST_BLOCK_NUMBER = new BigInteger("100");
    private static final String TRANSACTION_HASH = getRandomHexString();
    private static final String BLOCK_HASH = getRandomHexString();
    private static final String FROM = getRandomHexString();
    private static final String TO = getRandomHexString();
    private static final BigInteger VALUE = new BigInteger("1000");
    private static final WalrusTransaction TRANSACTION = WalrusTransaction.builder()
            .transactionHash(TRANSACTION_HASH)
            .from(FROM)
            .to(TO)
            .value(VALUE)
            .build();

    @BeforeEach
    void setUp() {
        when(web3Service.getLastBlockNumber()).thenReturn(LAST_BLOCK_NUMBER);
        when(web3Service.getTransactionByHash(TRANSACTION_HASH)).thenReturn(TRANSACTION);
    }

    @Test
    public void trackBlockConfirmationToTheMax() throws Exception {
        // given
        BigInteger lastConfirmedBlockNumber = LAST_BLOCK_NUMBER
                .subtract(BigInteger.valueOf(MAX_BLOCK_CONFIRMATION_COUNT))
                .subtract(BigInteger.TEN);
        int currentBlockConfirmationCount = 6;
        TransactionBlockConfirmationStatusEntity transactionBlockConfirmationStatusEntity
                = new TransactionBlockConfirmationStatusEntity();
        transactionBlockConfirmationStatusEntity.setTransactionHash(TRANSACTION_HASH);
        transactionBlockConfirmationStatusEntity.setLastConfirmedBlockNumber(lastConfirmedBlockNumber.toString());
        transactionBlockConfirmationStatusEntity.setCurrentBlockConfirmationCount(currentBlockConfirmationCount);
        transactionBlockConfirmationStatusEntity.setIsSettled(false);
        transactionBlockConfirmationStatusRepository.save(transactionBlockConfirmationStatusEntity);
        long timestamp = Instant.now().getEpochSecond();
        List<WalrusBlock> witnessBlockList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            WalrusBlock witnessBlock = WalrusBlock.builder()
                    .blockHash(BLOCK_HASH)
                    .blockNumber(lastConfirmedBlockNumber.add(BigInteger.valueOf(i)).add(BigInteger.ONE))
                    .timestamp(timestamp + i)
                    .transactions(Collections.emptyList())
                    .build();
            witnessBlockList.add(witnessBlock);
            when(web3Service.getBlockByNumber(witnessBlock.getBlockNumber())).thenReturn(witnessBlock);
        }

        // when
        blockConfirmationTrackingTaskManager.run();

        // then
        List<TaskEntity> taskEntityList = taskRepository.findAllByStatus(TaskStatus.READY);
        assertEquals(6, taskEntityList.size());
        List<BlockConfirmationTrackingTaskSpec> taskSpecList = extractTaskSpecList(taskEntityList);
        for (int i = 0; i < taskEntityList.size(); i++) {
            BlockConfirmationTrackingTaskSpec taskSpec = taskSpecList.get(i);
            assertEquals(i + 7, taskSpec.getBlockConfirmationCount());
            assertEquals(witnessBlockList.get(i), taskSpec.getWitnessBlock());
            assertEquals(TRANSACTION, taskSpec.getTransaction());
        }
    }

    @Test
    public void trackBlockConfirmationToMiddle() throws Exception {
        // given
        BigInteger lastConfirmedBlockNumber = LAST_BLOCK_NUMBER
                .subtract(BigInteger.valueOf(MAX_BLOCK_CONFIRMATION_COUNT))
                .add(BigInteger.valueOf(6));
        int currentBlockConfirmationCount = -1;
        TransactionBlockConfirmationStatusEntity transactionBlockConfirmationStatusEntity
                = new TransactionBlockConfirmationStatusEntity();
        transactionBlockConfirmationStatusEntity.setTransactionHash(TRANSACTION_HASH);
        transactionBlockConfirmationStatusEntity.setLastConfirmedBlockNumber(lastConfirmedBlockNumber.toString());
        transactionBlockConfirmationStatusEntity.setCurrentBlockConfirmationCount(currentBlockConfirmationCount);
        transactionBlockConfirmationStatusEntity.setIsSettled(false);
        transactionBlockConfirmationStatusRepository.save(transactionBlockConfirmationStatusEntity);
        long timestamp = Instant.now().getEpochSecond();
        List<WalrusBlock> witnessBlockList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            WalrusBlock witnessBlock = WalrusBlock.builder()
                    .blockHash(BLOCK_HASH)
                    .blockNumber(lastConfirmedBlockNumber.add(BigInteger.valueOf(i)).add(BigInteger.ONE))
                    .timestamp(timestamp + i)
                    .transactions(Collections.emptyList())
                    .build();
            witnessBlockList.add(witnessBlock);
            when(web3Service.getBlockByNumber(witnessBlock.getBlockNumber())).thenReturn(witnessBlock);
        }

        // when
        blockConfirmationTrackingTaskManager.run();

        // then
        List<TaskEntity> taskEntityList = taskRepository.findAllByStatus(TaskStatus.READY);
        assertEquals(6, taskEntityList.size());
        List<BlockConfirmationTrackingTaskSpec> taskSpecList = extractTaskSpecList(taskEntityList);
        for (int i = 0; i < taskEntityList.size(); i++) {
            BlockConfirmationTrackingTaskSpec taskSpec = taskSpecList.get(i);
            assertEquals(i, taskSpec.getBlockConfirmationCount());
            assertEquals(witnessBlockList.get(i), taskSpec.getWitnessBlock());
            assertEquals(TRANSACTION, taskSpec.getTransaction());
        }
    }

    private List<BlockConfirmationTrackingTaskSpec> extractTaskSpecList(List<TaskEntity> taskEntityList) {
        return taskEntityList.stream()
                .map(taskEntity -> {
                    try {
                        return objectMapper.readValue(taskEntity.getTaskSpec(), BlockConfirmationTrackingTaskSpec.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .sorted(Comparator.comparing(BlockConfirmationTrackingTaskSpec::getBlockConfirmationCount))
                .toList();
    }
}
