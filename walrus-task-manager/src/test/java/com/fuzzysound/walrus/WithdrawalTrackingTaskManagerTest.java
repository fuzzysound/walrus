package com.fuzzysound.walrus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fuzzysound.walrus.task.infrastructure.entity.LastConfirmedBlockNumberEntity;
import com.fuzzysound.walrus.task.infrastructure.entity.TaskEntity;
import com.fuzzysound.walrus.task.infrastructure.repository.LastConfirmedBlockNumberRepository;
import com.fuzzysound.walrus.task.infrastructure.repository.TaskRepository;
import com.fuzzysound.walrus.task.model.TaskStatus;
import com.fuzzysound.walrus.task.model.taskSpec.DepositTrackingTaskSpec;
import com.fuzzysound.walrus.task.model.taskSpec.WithdrawalTrackingTaskSpec;
import com.fuzzysound.walrus.task.service.TaskProgressService;
import com.fuzzysound.walrus.wallet.infrastructure.entity.WalletEntity;
import com.fuzzysound.walrus.wallet.infrastructure.repository.WalletRepository;
import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static com.fuzzysound.walrus.common.TestUtils.getRandomHexString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class WithdrawalTrackingTaskManagerTest extends TaskManagerTest {
    @Autowired private WithdrawalTrackingTaskManager withdrawalTrackingTaskManager;
    @Autowired private LastConfirmedBlockNumberRepository lastConfirmedBlockNumberRepository;
    @Autowired private TaskProgressService taskProgressService;
    @Autowired private WalletRepository walletRepository;
    @Autowired private TaskRepository taskRepository;
    @MockBean private BlockConfirmationTrackingTaskManager blockConfirmationTrackingTaskManager;
    @MockBean private DepositTrackingTaskManager depositTrackingTaskManager;
    private static final BigInteger LAST_BLOCK_NUMBER = new BigInteger("100");
    private static final BigInteger BLOCK_NUMBER_95 = new BigInteger("95");
    private static final BigInteger BLOCK_NUMBER_96 = new BigInteger("96");
    private static final String USER_A_WALLET_ADDRESS = getRandomHexString();
    private static final String USER_B_WALLET_ADDRESS = getRandomHexString();

    @BeforeEach
    void setUp() {
        createDefaultWalletEntity(USER_A_WALLET_ADDRESS);
        createDefaultWalletEntity(USER_B_WALLET_ADDRESS);
        when(web3Service.getLastBlockNumber()).thenReturn(LAST_BLOCK_NUMBER);
    }


    @Test
    public void whenTrackFirstTime() {
        // when
        withdrawalTrackingTaskManager.run();

        // then
        BigInteger lastConfirmedBlockNumber = taskProgressService.getLastConfirmedBlockNumber(
                WalrusTaskManagerKey.WITHDRAWAL_TRACKING.name()
        );
        assertEquals(LAST_BLOCK_NUMBER, lastConfirmedBlockNumber);
    }

    @Test
    public void whenTrackInMiddle() {
        // given
        BigInteger lastConfirmedBlockNumber = new BigInteger("95");
        LastConfirmedBlockNumberEntity lastConfirmedBlockNumberEntity
                = new LastConfirmedBlockNumberEntity();
        lastConfirmedBlockNumberEntity.setDistKey(WalrusTaskManagerKey.WITHDRAWAL_TRACKING.name());
        lastConfirmedBlockNumberEntity.setBlockNumber(lastConfirmedBlockNumber.toString());
        lastConfirmedBlockNumberRepository.save(lastConfirmedBlockNumberEntity);

        WalrusTransaction outOfRangeTransaction = WalrusTransaction.builder()
                .transactionHash(getRandomHexString())
                .from(USER_A_WALLET_ADDRESS)
                .to(USER_B_WALLET_ADDRESS)
                .value(BigInteger.ONE)
                .build();
        WalrusBlock outOfRangeBlock = WalrusBlock.builder()
                .blockHash(getRandomHexString())
                .blockNumber(new BigInteger("95"))
                .timestamp(Instant.now().getEpochSecond())
                .transactions(List.of(outOfRangeTransaction))
                .build();
        when(web3Service.getBlockByNumber(BLOCK_NUMBER_95)).thenReturn(outOfRangeBlock);

        WalrusTransaction userAToBTransaction = WalrusTransaction.builder()
                .transactionHash(getRandomHexString())
                .from(USER_A_WALLET_ADDRESS)
                .to(USER_B_WALLET_ADDRESS)
                .value(BigInteger.ONE)
                .build();
        WalrusBlock userAToBTransactionBlock = WalrusBlock.builder()
                .blockHash(getRandomHexString())
                .blockNumber(BLOCK_NUMBER_96)
                .timestamp(Instant.now().getEpochSecond())
                .transactions(List.of(userAToBTransaction))
                .build();
        when(web3Service.getBlockByNumber(BLOCK_NUMBER_96)).thenReturn(userAToBTransactionBlock);

        WalrusTransaction userBToATransaction = WalrusTransaction.builder()
                .transactionHash(getRandomHexString())
                .from(USER_B_WALLET_ADDRESS)
                .to(USER_A_WALLET_ADDRESS)
                .value(BigInteger.ONE)
                .build();
        WalrusBlock userBToATransactionBlock = WalrusBlock.builder()
                .blockHash(getRandomHexString())
                .blockNumber(LAST_BLOCK_NUMBER)
                .timestamp(Instant.now().getEpochSecond())
                .transactions(List.of(userBToATransaction))
                .build();
        when(web3Service.getBlockByNumber(LAST_BLOCK_NUMBER)).thenReturn(userBToATransactionBlock);


        // when
        withdrawalTrackingTaskManager.run();

        // then
        List<TaskEntity> taskEntitiyList = taskRepository.findAllByStatus(TaskStatus.READY);
        assertEquals(2, taskEntitiyList.size());
        List<WithdrawalTrackingTaskSpec> taskSpecList = extractTaskSpecList(taskEntitiyList);
        assertEquals(userAToBTransactionBlock, taskSpecList.get(0).getBlock());
        assertEquals(userAToBTransaction, taskSpecList.get(0).getTransaction());
        assertEquals(userBToATransactionBlock, taskSpecList.get(1).getBlock());
        assertEquals(userBToATransaction, taskSpecList.get(1).getTransaction());
    }

    private void createDefaultWalletEntity(String address) {
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setAddress(address);
        walletEntity.setUsername(getRandomHexString());
        walletEntity.setBase64Password("pass");
        walletEntity.setFilePath(getRandomHexString());
        walletEntity.setBalance("0");
        walletRepository.save(walletEntity);
    }

    private List<WithdrawalTrackingTaskSpec> extractTaskSpecList(List<TaskEntity> taskEntityList) {
        return taskEntityList.stream()
                .map(taskEntity -> {
                    try {
                        return objectMapper.readValue(taskEntity.getTaskSpec(), WithdrawalTrackingTaskSpec.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .sorted(Comparator.comparing(taskSpec -> taskSpec.getBlock().getBlockNumber()))
                .toList();
    }
}
