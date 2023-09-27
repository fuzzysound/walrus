package com.fuzzysound.walrus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuzzysound.walrus.common.exception.TransactionFailedException;
import com.fuzzysound.walrus.dto.EventHistoryResponseDto;
import com.fuzzysound.walrus.dto.TransactionRequestDto;
import com.fuzzysound.walrus.dto.WalletResponseDto;
import com.fuzzysound.walrus.event.model.TransactionStatus;
import com.fuzzysound.walrus.facade.WalrusFacade;
import com.fuzzysound.walrus.wallet.model.WalrusWallet;
import com.fuzzysound.walrus.wallet.model.WalrusWalletFileInfo;
import com.fuzzysound.walrus.wallet.service.WalletFileService;
import com.fuzzysound.walrus.web3.model.WalrusBlock;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import com.fuzzysound.walrus.web3.service.Web3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.fuzzysound.walrus.common.TestUtils.getRandomHexString;
import static com.fuzzysound.walrus.common.Web3Constants.MAX_BLOCK_CONFIRMATION_COUNT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ApiIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private WalrusFacade walrusFacade;
    @MockBean private Web3Service web3Service;
    @MockBean private WalletFileService walletFileService;
    protected static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void hello() throws Exception {
        // when
        ResultActions resultActions= mvc.perform(get("/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isOk());
    }

    @Test
    public void createAndGetWallet() throws Exception {
        // given
        String username = "user";
        String password = "pass";
        String address = getRandomHexString();
        String walletFilePath = "/wallet/file/path";

        // 테스트 1. 지갑 생성
        // given
        WalrusWalletFileInfo walletFileInfo = WalrusWalletFileInfo.builder()
                .filePath(walletFilePath)
                .address(address)
                .build();
        when(walletFileService.createWalletFile(username, password)).thenReturn(walletFileInfo);

        // when
        WalletResponseDto walletResponseDtoFromPost = objectMapper.readValue(
                requestAndGetContent("post", "/wallet", null, username, password),
                WalletResponseDto.class
        );

        // then
        assertEquals(username, walletResponseDtoFromPost.getUsername());
        assertEquals(address, walletResponseDtoFromPost.getAddress());
        assertEquals(BigInteger.ZERO, new BigInteger(walletResponseDtoFromPost.getBalance()));

        // 테스트 2. 지갑 가져오기
        // when
        WalletResponseDto walletResponseDtoFromGet = objectMapper.readValue(
                requestAndGetContent("get", "/wallet", null, username, password),
                WalletResponseDto.class
        );
        assertEquals(walletResponseDtoFromPost, walletResponseDtoFromGet);

        // 테스트 3. 잘못된 패스워드 입력
        // given
        String wrongPassword = "wrongpass";

        // when
        ResultActions wrongPasswordResultActions = request("get", "/wallet", null, username, wrongPassword);

        // then
        wrongPasswordResultActions
                .andExpect(status().isForbidden());

        // 테스트 4. 잘못된 유저네임 입력
        // given
        String wrongUsername = "wronguser";

        // when
        ResultActions wrongUsernameResultActions = request("get", "/wallet", null, wrongUsername, password);

        // then
        wrongUsernameResultActions
                .andExpect(status().isBadRequest());

    }

    @Test
    public void transaction() throws Exception {
        // given
        String userA = "userA";
        String userB = "userB";
        String password = "pass";
        String userAWalletAddress = getRandomHexString();
        String userBWalletAddress = getRandomHexString();
        String userAWalletFilePath = "/wallet/A/file/path";
        String userBWalletFilePath = "/wallet/B/file/path";
        WalrusWalletFileInfo userAWalletFileInfo = WalrusWalletFileInfo.builder()
                .filePath(userAWalletFilePath)
                .address(userAWalletAddress)
                .build();
        WalrusWalletFileInfo userBWalletFileInfo = WalrusWalletFileInfo.builder()
                .filePath(userBWalletFilePath)
                .address(userBWalletAddress)
                .build();
        BigInteger transactionCost = new BigInteger("1000");
        byte[] signedTransaction = getRandomHexString().getBytes();
        when(web3Service.getSignedTransaction(any(WalrusWallet.class), anyString(), any(BigInteger.class)))
                .thenReturn(signedTransaction);
        when(web3Service.getTransactionCost()).thenReturn(transactionCost);
        when(walletFileService.createWalletFile(userA, password)).thenReturn(userAWalletFileInfo);
        when(walletFileService.createWalletFile(userB, password)).thenReturn(userBWalletFileInfo);

        // 테스트 1. 지갑 두 개 생성
        // when
        WalletResponseDto userAWallet = objectMapper.readValue(
                requestAndGetContent("post", "/wallet", null, userA, password),
                WalletResponseDto.class
        );
        WalletResponseDto userBWallet = objectMapper.readValue(
                requestAndGetContent("post", "/wallet", null, userB, password),
                WalletResponseDto.class
        );

        // then
        assertEquals(userA, userAWallet.getUsername());
        assertEquals(userAWalletAddress, userAWallet.getAddress());
        assertEquals(userB, userBWallet.getUsername());
        assertEquals(userBWalletAddress, userBWallet.getAddress());

        // 테스트 2. 외부 지갑에서 유저 A 지갑으로의 입금
        // given
        // 수동 입금처리
        String initialDepositWei = "1000000000000000000"; // 1 ether
        String externalTransactionHash = getRandomHexString();
        BigInteger externalBlockNumber = new BigInteger("1000");
        long externalBlockTimestamp = Instant.now().getEpochSecond() - 100;
        WalrusTransaction externalTransaction = WalrusTransaction.builder()
                .transactionHash(externalTransactionHash)
                .from(getRandomHexString())
                .to(userAWallet.getAddress())
                .value(new BigInteger(initialDepositWei))
                .build();
        WalrusBlock externalTransactionBlock = WalrusBlock.builder()
                .blockHash(getRandomHexString())
                .blockNumber(externalBlockNumber)
                .timestamp(externalBlockTimestamp)
                .transactions(List.of(externalTransaction))
                .build();
        walrusFacade.postDeposit(externalTransactionBlock, externalTransaction);

        // Block confirmation 을 최대치까지 진행
        when(web3Service.getTransactionByHash(externalTransactionHash)).thenReturn(externalTransaction);
        for (int blockConfirmationCount = 0; blockConfirmationCount <= MAX_BLOCK_CONFIRMATION_COUNT; blockConfirmationCount++) {
            BigInteger witnessBlockNumber = externalBlockNumber.add(BigInteger.valueOf(blockConfirmationCount)).add(BigInteger.ONE);
            long witnessBlockTimestamp = externalBlockTimestamp + (long) (blockConfirmationCount + 1);
            WalrusBlock witnessBlock = WalrusBlock.builder()
                    .blockHash(getRandomHexString())
                    .blockNumber(witnessBlockNumber)
                    .timestamp(witnessBlockTimestamp)
                    .transactions(Collections.emptyList())
                    .build();
            walrusFacade.updateBlockConfirmation(witnessBlock, externalTransaction, blockConfirmationCount);
        }

        // when
        userAWallet = objectMapper.readValue(
                requestAndGetContent("get", "/wallet", null, userA, password),
                WalletResponseDto.class
        );

        // then
        assertEquals(BigInteger.ONE, new BigInteger(userAWallet.getBalance()));

        // 테스트 3. 잔액을 초과하는 출금 시도
        // given

        // when
        TransactionRequestDto overWithdrawalTransactionRequestDto = TransactionRequestDto.builder()
                .to(userBWalletAddress)
                .value("2")
                .build();
        ResultActions overWithdrawalResultActions = request("post", "/transaction", overWithdrawalTransactionRequestDto, userA, password);

        // then
        overWithdrawalResultActions
                .andExpect(status().isBadRequest());

        // 테스트 4. 정상적인 출금 시도
        // given
        BigInteger transferValue = new BigInteger("500000000000000000"); // 0.5 ether
        String blockHash = "0x1111111111";
        BigInteger blockNumber = new BigInteger("1111");
        long blockTimestamp = Instant.now().getEpochSecond();
        String transactionHash = getRandomHexString();
        when(web3Service.getTransactionHash(signedTransaction)).thenReturn(transactionHash);

        WalrusTransaction transaction = WalrusTransaction.builder()
                .transactionHash(transactionHash)
                .from(userAWalletAddress)
                .to(userBWalletAddress)
                .value(transferValue)
                .build();
        WalrusBlock block = WalrusBlock.builder()
                .blockHash(blockHash)
                .blockNumber(blockNumber)
                .timestamp(blockTimestamp)
                .transactions(List.of(transaction))
                .build();
        CompletableFuture<Pair<WalrusBlock, WalrusTransaction>> transactionFuture
                = CompletableFuture.completedFuture(Pair.of(block, transaction));
        when(web3Service.sendSignedTransaction(signedTransaction)).thenReturn(transactionFuture);

        // when
        String transferEther = "0.5";
        TransactionRequestDto withdrawalTransactionRequestDto = TransactionRequestDto.builder()
                .to(userBWalletAddress)
                .value(transferEther)
                .build();
        ResultActions withdrawalResultActions = request("post", "/transaction", withdrawalTransactionRequestDto, userA, password);

        // then
        withdrawalResultActions
                .andExpect(status().isOk());

        // 유저 B로의 입금 후처리
        walrusFacade.postDeposit(block, transaction);

        // 테스트 5. 실질적 잔액 (잔액 - 출금 진행중 잔액)을 초과하는 출금 시도
        // given
        ResultActions secondOverWithdrawalResultActions = request("post", "/transaction", withdrawalTransactionRequestDto, userA, password);

        // then
        secondOverWithdrawalResultActions
                .andExpect(status().isBadRequest());

        // 테스트 6. 유저 B로부터의 출금 시도 (유저 B는 입금을 받았으나 트랜잭션이 confirm되지 않음)
        // when
        TransactionRequestDto userBWithdrawalTransactionRequestDto = TransactionRequestDto.builder()
                .to(userAWalletAddress)
                .value("0.1")
                .build();
        ResultActions userBWithdrawalResultActions = request("post", "/transaction", userBWithdrawalTransactionRequestDto, userB, password);

        // then
        userBWithdrawalResultActions
                .andExpect(status().isBadRequest());

        // 테스트 7. Block confirmation 완료 후 입금 및 출금 완료 확인
        // Block confirmation 을 최대치까지 진행
        when(web3Service.getTransactionByHash(transactionHash)).thenReturn(transaction);
        for (int blockConfirmationCount = 0; blockConfirmationCount <= MAX_BLOCK_CONFIRMATION_COUNT; blockConfirmationCount++) {
            BigInteger witnessBlockNumber = blockNumber.add(BigInteger.valueOf(blockConfirmationCount)).add(BigInteger.ONE);
            long witnessBlockTimestamp = blockTimestamp + (long) (blockConfirmationCount + 1);
            WalrusBlock witnessBlock = WalrusBlock.builder()
                    .blockHash(blockHash)
                    .blockNumber(witnessBlockNumber)
                    .timestamp(witnessBlockTimestamp)
                    .transactions(Collections.emptyList())
                    .build();
            walrusFacade.updateBlockConfirmation(witnessBlock, transaction, blockConfirmationCount);
        }

        // when
        userAWallet = objectMapper.readValue(
                requestAndGetContent("get", "/wallet", null, userA, password),
                WalletResponseDto.class
        );
        userBWallet = objectMapper.readValue(
                requestAndGetContent("get", "/wallet", null, userB, password),
                WalletResponseDto.class
        );
        BigInteger expectedUserABalance = (new BigInteger(initialDepositWei))
                .subtract(new BigDecimal(transferEther).multiply(BigDecimal.TEN.pow(18)).toBigInteger())
                .subtract(transactionCost);
        BigInteger actualUserABalance = new BigDecimal(userAWallet.getBalance()).multiply(BigDecimal.TEN.pow(18)).toBigInteger();
        assertEquals(expectedUserABalance, actualUserABalance);
        BigInteger expectedUserBBalance = new BigDecimal(transferEther).multiply(BigDecimal.TEN.pow(18)).toBigInteger();
        BigInteger actualUserBBalance = new BigDecimal(userBWallet.getBalance()).multiply(BigDecimal.TEN.pow(18)).toBigInteger();
        assertEquals(expectedUserBBalance, actualUserBBalance);

        // 테스트 8. 입출금 이벤트 전체 조회
        // when
        EventHistoryResponseDto userAEventHistoryResponse = objectMapper.readValue(
                requestAndGetContent("get", "/eventHistory?size=100", null, userA, password),
                EventHistoryResponseDto.class
        );
        EventHistoryResponseDto userBEventHistoryResponse = objectMapper.readValue(
                requestAndGetContent("get", "/eventHistory?size=100", null, userB, password),
                EventHistoryResponseDto.class
        );

        // then
        assertEquals(28, userAEventHistoryResponse.getEventHistory().size());
        for (int i = 0; i < 14; i++) {
            EventHistoryResponseDto.EventHistoryDto eventHistoryDto = userAEventHistoryResponse.getEventHistory().get(i);
            assertEquals(EventHistoryResponseDto.EventType.WITHDRAWAL, eventHistoryDto.getEventType());
            assertEquals(12 - i, eventHistoryDto.getBlockConfirmationCount());
            if (i == 0) {
                assertEquals(TransactionStatus.CONFIRMED, eventHistoryDto.getTransactionStatus());
            } else if (i == 13) {
                assertEquals(TransactionStatus.PENDING, eventHistoryDto.getTransactionStatus());
            } else {
                assertEquals(TransactionStatus.MINED, eventHistoryDto.getTransactionStatus());
            }
        }
        for (int i = 0; i < 14; i++) {
            EventHistoryResponseDto.EventHistoryDto eventHistoryDto = userAEventHistoryResponse.getEventHistory().get(i + 14);
            assertEquals(EventHistoryResponseDto.EventType.DEPOSIT, eventHistoryDto.getEventType());
            assertEquals(12 - i, eventHistoryDto.getBlockConfirmationCount());
            if (i == 0) {
                assertEquals(TransactionStatus.CONFIRMED, eventHistoryDto.getTransactionStatus());
            } else if (i == 13) {
                assertEquals(TransactionStatus.PENDING, eventHistoryDto.getTransactionStatus());
            } else {
                assertEquals(TransactionStatus.MINED, eventHistoryDto.getTransactionStatus());
            }
        }
        assertEquals(14, userBEventHistoryResponse.getEventHistory().size());
        for (int i = 0; i < 14; i++) {
            EventHistoryResponseDto.EventHistoryDto eventHistoryDto = userBEventHistoryResponse.getEventHistory().get(i);
            assertEquals(EventHistoryResponseDto.EventType.DEPOSIT, eventHistoryDto.getEventType());
            assertEquals(12 - i, eventHistoryDto.getBlockConfirmationCount());
            if (i == 0) {
                assertEquals(TransactionStatus.CONFIRMED, eventHistoryDto.getTransactionStatus());
            } else if (i == 13) {
                assertEquals(TransactionStatus.PENDING, eventHistoryDto.getTransactionStatus());
            } else {
                assertEquals(TransactionStatus.MINED, eventHistoryDto.getTransactionStatus());
            }
        }

        // 테스트 9. 입출금 이벤트 시간 파라미터로 조회
        // when
        long startingAfter = blockTimestamp + 3;
        long endingBefore = blockTimestamp + 9;
        String eventHistoryUri = "/eventHistory?starting_after=" + startingAfter + "&ending_before=" + endingBefore + "&size=100";
        userAEventHistoryResponse = objectMapper.readValue(
                requestAndGetContent("get", eventHistoryUri, null, userA, password),
                EventHistoryResponseDto.class
        );
        userBEventHistoryResponse = objectMapper.readValue(
                requestAndGetContent("get", eventHistoryUri, null, userB, password),
                EventHistoryResponseDto.class
        );

        // then
        assertEquals(5, userAEventHistoryResponse.getEventHistory().size());
        for (int i = 0; i < 5; i++) {
            EventHistoryResponseDto.EventHistoryDto eventHistoryDto = userAEventHistoryResponse.getEventHistory().get(i);
            assertEquals(EventHistoryResponseDto.EventType.WITHDRAWAL, eventHistoryDto.getEventType());
            assertEquals(7 - i, eventHistoryDto.getBlockConfirmationCount());
            assertEquals(TransactionStatus.MINED, eventHistoryDto.getTransactionStatus());
        }
        assertEquals(5, userBEventHistoryResponse.getEventHistory().size());
        for (int i = 0; i < 5; i++) {
            EventHistoryResponseDto.EventHistoryDto eventHistoryDto = userBEventHistoryResponse.getEventHistory().get(i);
            assertEquals(EventHistoryResponseDto.EventType.DEPOSIT, eventHistoryDto.getEventType());
            assertEquals(7 - i, eventHistoryDto.getBlockConfirmationCount());
            assertEquals(TransactionStatus.MINED, eventHistoryDto.getTransactionStatus());
        }

        // 테스트 10. 트랜잭션 실패 후 재시도
        // 잔액의 2/3 만큼 출금을 시도할 경우, 이중 출금이 되지 않는다면 첫 시도가 실패하더라도 두번째 시도는 성공해야 함
        // given
        BigDecimal userABalance = new BigDecimal(userAWallet.getBalance());
        BigDecimal firstFailTransactionTransferValue = userABalance.multiply(BigDecimal.valueOf(0.66));
        String firstFailTransactionHash = getRandomHexString();
        when(web3Service.getTransactionHash(signedTransaction)).thenReturn(firstFailTransactionHash);

        // 트랜잭션 에러 모킹
        CompletableFuture<Pair<WalrusBlock, WalrusTransaction>> failedTransactionFuture
                = CompletableFuture.failedFuture(new TransactionFailedException());
        when(web3Service.sendSignedTransaction(signedTransaction)).thenReturn(failedTransactionFuture);

        // 첫 시도
        TransactionRequestDto firstFailTransactionRequestDto = TransactionRequestDto.builder()
                .to(userBWalletAddress)
                .value(firstFailTransactionTransferValue.toString())
                .build();
        ResultActions firstFailTransactionResultActions = request("post", "/transaction", firstFailTransactionRequestDto, userA, password);

        // future가 끝나도록 기다림
        Thread.sleep(500);

        // 트랜잭션 정상 작동하도록 복구

        // 두번째 시도
        String secondSucceedTransactionHash = getRandomHexString();
        WalrusTransaction secondSucceedTransaction = WalrusTransaction.builder()
                .transactionHash(secondSucceedTransactionHash)
                .from(userAWalletAddress)
                .to(userBWalletAddress)
                .value(firstFailTransactionTransferValue.multiply(BigDecimal.TEN.pow(18)).toBigInteger())
                .build();
        WalrusBlock secondSucceedBlock = WalrusBlock.builder()
                .blockHash(getRandomHexString())
                .blockNumber(blockNumber.add(BigInteger.valueOf(100)))
                .timestamp(Instant.now().getEpochSecond())
                .transactions(List.of(secondSucceedTransaction))
                .build();
        when(web3Service.getTransactionHash(signedTransaction)).thenReturn(secondSucceedTransactionHash);

        // 트랜잭션 성공 모킹
        CompletableFuture<Pair<WalrusBlock, WalrusTransaction>> secondSucceedTransactionFuture
                = CompletableFuture.completedFuture(Pair.of(secondSucceedBlock, secondSucceedTransaction));
        when(web3Service.sendSignedTransaction(signedTransaction)).thenReturn(secondSucceedTransactionFuture);

        // when
        ResultActions secondSucceedTransactionResultActions = request("post", "/transaction", firstFailTransactionRequestDto, userA, password);

        // then
        secondSucceedTransactionResultActions
                .andExpect(status().isOk());

    }

    private ResultActions request(String method, String uri, Object data, String username, String password) throws Exception {
        if (method.equals("get")) {
            return mvc.perform(get(uri)
                    .header("X-username", username)
                    .header("X-password", password)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON));
        } else if (method.equals("post")) {
            String body = (data == null) ? "" : objectMapper.writeValueAsString(data);
            return mvc.perform(post(uri)
                    .content(body)
                    .header("X-username", username)
                    .header("X-password", password)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON));
        } else {
            return null;
        }
    }

    private String getContent(ResultActions resultActions) throws Exception {
        return resultActions.andReturn().getResponse().getContentAsString();
    }

    private String requestAndGetContent(String method, String uri, Object data, String username, String password) throws Exception {
        ResultActions resultActions = request(method, uri, data, username, password);
        if (resultActions == null) {
            return "";
        } else {
            return getContent(resultActions);
        }
    }
}
