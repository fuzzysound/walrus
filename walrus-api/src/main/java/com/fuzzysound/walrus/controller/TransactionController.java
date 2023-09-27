package com.fuzzysound.walrus.controller;

import com.fuzzysound.walrus.ValidationException;
import com.fuzzysound.walrus.dto.TransactionRequestDto;
import com.fuzzysound.walrus.facade.WalrusFacade;
import com.fuzzysound.walrus.provider.AuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final AuthenticationProvider authenticationProvider;
    private final WalrusFacade walrusFacade;

    @PostMapping("transaction")
    public String createTransaction(@RequestBody TransactionRequestDto transactionRequestDto) {
        validate(transactionRequestDto.getValue());
        BigInteger weiValue = Convert.toWei(transactionRequestDto.getValue(), Convert.Unit.ETHER).toBigInteger();
        walrusFacade.withdraw(authenticationProvider.getUsername(), authenticationProvider.getPassword(),
                transactionRequestDto.getTo(), weiValue);
        return "Pending";
    }

    private void validate(String rawValue) {
        try {
            BigDecimal value = new BigDecimal(rawValue);
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("value must be non-negative.");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("value must be a number.");
        }
    }
}
