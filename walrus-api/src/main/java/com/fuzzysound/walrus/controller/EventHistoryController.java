package com.fuzzysound.walrus.controller;

import com.fuzzysound.walrus.ValidationException;
import com.fuzzysound.walrus.converter.DtoConverter;
import com.fuzzysound.walrus.dto.EventHistoryResponseDto;
import com.fuzzysound.walrus.provider.AuthenticationProvider;
import com.fuzzysound.walrus.event.model.EventHistory;
import com.fuzzysound.walrus.event.service.EventHistoryService;
import com.fuzzysound.walrus.wallet.model.WalrusWallet;
import com.fuzzysound.walrus.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventHistoryController {
    private final AuthenticationProvider authenticationProvider;
    private final WalletService walletService;
    private final EventHistoryService eventHistoryService;
    private final DtoConverter dtoConverter;

    private static final String DEFAULT_SIZE = "10";
    private static final Integer MAX_SIZE = 100;

    @GetMapping("eventHistory")
    public EventHistoryResponseDto getEventHistories(
            @RequestParam(value = "starting_after", required = false) Long startingAfter,
            @RequestParam(value = "ending_before", required = false) Long endingBefore,
            @RequestParam(value = "size", defaultValue = DEFAULT_SIZE) int size
            ) {
        validate(startingAfter, endingBefore, size);
        WalrusWallet wallet = walletService.getWallet(authenticationProvider.getUsername(), authenticationProvider.getPassword());
        List<EventHistory> eventHistories = (startingAfter == null || endingBefore == null)
                ? eventHistoryService.getEventHistories(wallet.getAddress(), size)
                : eventHistoryService.getEventHistories(wallet.getAddress(), startingAfter, endingBefore, size);
        return dtoConverter.toEventHistoryResponseDto(eventHistories);
    }

    private void validate(Long startingAfter, Long endingBefore, int size) {
        if (startingAfter != null && endingBefore != null
        && startingAfter > endingBefore) {
            throw new ValidationException("starting_after must be less than ending_before.");
        }
        if (size < 0 || size > MAX_SIZE) {
            throw new ValidationException("size must be between 0 and " + MAX_SIZE);
        }
    }
}
