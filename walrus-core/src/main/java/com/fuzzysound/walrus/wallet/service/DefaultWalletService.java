package com.fuzzysound.walrus.wallet.service;

import com.fuzzysound.walrus.common.exception.*;
import com.fuzzysound.walrus.wallet.infrastructure.converter.WalletEntityConverter;
import com.fuzzysound.walrus.wallet.infrastructure.entity.PendingWithdrawalEntity;
import com.fuzzysound.walrus.wallet.infrastructure.entity.WalletEntity;
import com.fuzzysound.walrus.wallet.infrastructure.repository.PendingWithdrawalRepository;
import com.fuzzysound.walrus.wallet.infrastructure.repository.WalletRepository;
import com.fuzzysound.walrus.wallet.model.WalrusWallet;
import com.fuzzysound.walrus.wallet.model.WalrusWalletFileInfo;
import com.fuzzysound.walrus.wallet.model.WithdrawalStatus;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import com.fuzzysound.walrus.web3.service.Web3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultWalletService implements WalletService {
    private final Web3Service web3Service;
    private final WalletFileService walletFileService;
    private final WalletRepository walletRepository;
    private final PendingWithdrawalRepository pendingWithdrawalRepository;
    private final WalletEntityConverter walletEntityConverter;

    private static final String TRANSACTION_PROCESS_EXCEPTION_MSG =
            "Transaction cannot be processed because value is larger than balance, " +
                    "transactionHash: %s, value: %s, balance: %s";
    private static final String WITHDRAWAL_EXCEPTION_MSG =
            "Withdrawal cannot be processed because value plus transaction cost is larger than balance minus pending balance, " +
                    "value: %s, transaction cost: %s, balance: %s, pending balance: %s";

    @Override
    public List<WalrusTransaction> filterTransactionsDepositToWalrus(List<WalrusTransaction> transactions) {
        List<String> addressList = transactions.stream().map(WalrusTransaction::getTo).toList();
        Set<String> walrusAddressSet = walletRepository.findAllByAddressIn(addressList).stream()
                .map(WalletEntity::getAddress).collect(Collectors.toSet());
        return transactions.stream()
                .filter(transaction -> walrusAddressSet.contains(transaction.getTo()))
                .toList();

    }

    @Override
    public List<WalrusTransaction> filterTransactionWithdrawFromWalrus(List<WalrusTransaction> transactions) {
        List<String> addressList = transactions.stream().map(WalrusTransaction::getFrom).toList();
        Set<String> walrusAddressSet = walletRepository.findAllByAddressIn(addressList).stream()
                .map(WalletEntity::getAddress).collect(Collectors.toSet());
        return transactions.stream()
                .filter(transaction -> walrusAddressSet.contains(transaction.getFrom()))
                .toList();
    }

    @Override
    @Transactional
    public void processConfirmedTransaction(String transactionHash) {
        processConfirmedTransactionDeposit(transactionHash);
        processConfirmedTransactionWithdrawal(transactionHash);
    }

    private void processConfirmedTransactionDeposit(String transactionHash) {
        WalrusTransaction transaction = web3Service.getTransactionByHash(transactionHash);
        Optional<WalletEntity> toWalletEntityOptional = walletRepository.findByAddressForUpdate(transaction.getTo());
        if (toWalletEntityOptional.isPresent()) {
            WalletEntity toWalletEntity = toWalletEntityOptional.get();
            BigInteger balance = new BigInteger(toWalletEntity.getBalance());
            BigInteger newBalance = balance.add(transaction.getValue());
            toWalletEntity.setBalance(newBalance.toString());
        }
    }

    private void processConfirmedTransactionWithdrawal(String transactionHash) {
        Optional<PendingWithdrawalEntity> pendingWithdrawalEntityOptional
                = pendingWithdrawalRepository.findByTransactionHash(transactionHash);
        if (pendingWithdrawalEntityOptional.isEmpty()) {
            return;
        }
        PendingWithdrawalEntity pendingWithdrawalEntity = pendingWithdrawalEntityOptional.get();
        if (!pendingWithdrawalEntity.getWithdrawalStatus().equals(WithdrawalStatus.PENDING)) {
            return;
        }
        Optional<WalletEntity> fromWalletEntityOptional = walletRepository.findByAddressForUpdate(
                pendingWithdrawalEntity.getAddress());
        if (fromWalletEntityOptional.isPresent()) {
            WalletEntity fromWalletEntity = fromWalletEntityOptional.get();
            BigInteger balance = new BigInteger(fromWalletEntity.getBalance());
            BigInteger pendingBalance = new BigInteger(pendingWithdrawalEntity.getPendingValue());
            if (balance.compareTo(pendingBalance) < 0) {
                throw new ConsistencyException(
                        String.format(TRANSACTION_PROCESS_EXCEPTION_MSG,
                                transactionHash, pendingBalance, balance)
                );
            } else {
                BigInteger newBalance = balance.subtract(pendingBalance);
                fromWalletEntity.setBalance(newBalance.toString());
            }
        }
        pendingWithdrawalEntity.setWithdrawalStatus(WithdrawalStatus.CONFIRMED);
    }

    @Override
    @Transactional
    public WalrusWallet createWallet(String username, String password) {
        Optional<WalletEntity> walletEntityOptional = walletRepository.findByUsername(username);
        if (walletEntityOptional.isPresent()) {
            throw new UsernameExistsException("Username " + username + " already exists.");
        }
        WalrusWalletFileInfo walletFileInfo = walletFileService.createWalletFile(username, password);
        WalrusWallet wallet = WalrusWallet.builder()
                .username(username)
                .password(password)
                .filePath(walletFileInfo.getFilePath())
                .address(walletFileInfo.getAddress())
                .balance(BigInteger.ZERO)
                .build();
        WalletEntity walletEntity = walletEntityConverter.toEntity(wallet);
        walletRepository.save(walletEntity);
        log.info("Wallet created for username {}, address {}.", username, wallet.getAddress());
        return wallet;
    }

    @Override
    public WalrusWallet getWallet(String username, String password) {
        Optional<WalletEntity> walletEntityOptional = walletRepository.findByUsername(username);
        if (walletEntityOptional.isPresent()) {
            WalletEntity walletEntity = walletEntityOptional.get();
            String base64Password = new String(Base64.getEncoder().encode(password.getBytes()));
            if (base64Password.equals(walletEntity.getBase64Password())) {
                return walletEntityConverter.fromEntity(walletEntity);
            } else {
                throw new AuthorizationFailedException("Wrong password.");
            }
        } else {
            throw new NoWalletException("Wallet does not exist.");
        }
    }

    @Override
    @Transactional
    public void prepareWithdrawal(String transactionHash, String fromAddress, BigInteger value) {
        Optional<WalletEntity> walletEntityOptional = walletRepository.findByAddressForUpdate(fromAddress);
        if (walletEntityOptional.isPresent()) {
            WalletEntity walletEntity = walletEntityOptional.get();
            List<PendingWithdrawalEntity> pendingWithdrawalEntityList
                    = pendingWithdrawalRepository.findAllByAddress(fromAddress);
            BigInteger balance = new BigInteger(walletEntity.getBalance());
            BigInteger pendingBalance = getTotalPendingBalance(pendingWithdrawalEntityList);
            BigInteger realBalance = balance.subtract(pendingBalance);
            BigInteger transactionCost = web3Service.getTransactionCost();
            BigInteger realValue = value.add(transactionCost);
            if (realBalance.compareTo(realValue) < 0) {
                throw new ConsistencyException(
                        String.format(WITHDRAWAL_EXCEPTION_MSG, value, transactionCost, balance, pendingBalance)
                );
            }
            PendingWithdrawalEntity newPendingWithDrawalEntity = new PendingWithdrawalEntity();
            newPendingWithDrawalEntity.setTransactionHash(transactionHash);
            newPendingWithDrawalEntity.setAddress(fromAddress);
            newPendingWithDrawalEntity.setPendingValue(realValue.toString());
            newPendingWithDrawalEntity.setWithdrawalStatus(WithdrawalStatus.REQUESTED);
            pendingWithdrawalRepository.save(newPendingWithDrawalEntity);
        } else {
            throw new NoWalletException("Wallet does not exist.");
        }
    }

    @Override
    @Transactional
    public void finishWithdrawal(String transactionHash) {
        Optional<PendingWithdrawalEntity> pendingWithdrawalEntityOptional
                = pendingWithdrawalRepository.findByTransactionHash(transactionHash);
        if (pendingWithdrawalEntityOptional.isEmpty()) {
            log.warn("Tried to finish withdrawal but no such pending withdrawal exists.");
            return;
        }
        PendingWithdrawalEntity pendingWithdrawalEntity = pendingWithdrawalEntityOptional.get();
        pendingWithdrawalEntity.setWithdrawalStatus(WithdrawalStatus.PENDING);
    }

    @Override
    @Transactional
    public void undoWithdrawal(String transactionHash) {
        Optional<PendingWithdrawalEntity> pendingWithdrawalEntityOptional
                = pendingWithdrawalRepository.findByTransactionHash(transactionHash);
        if (pendingWithdrawalEntityOptional.isEmpty()) {
            return;
        }
        PendingWithdrawalEntity pendingWithdrawalEntity = pendingWithdrawalEntityOptional.get();
        pendingWithdrawalEntity.setWithdrawalStatus(WithdrawalStatus.CANCELLED);
    }

    @Override
    public boolean addressExists(String address) {
        Optional<WalletEntity> walletEntityOptional = walletRepository.findByAddress(address);
        return walletEntityOptional.isPresent();
    }

    private BigInteger getTotalPendingBalance(List<PendingWithdrawalEntity> pendingWithdrawalEntityList) {
        return pendingWithdrawalEntityList.stream()
                .filter(entity -> entity.getWithdrawalStatus().isOnProcess())
                .map(entity -> new BigInteger(entity.getPendingValue()))
                .reduce(BigInteger.ZERO, BigInteger::add);
    }
}
