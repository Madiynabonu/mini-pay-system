package com.application.minipay.service;

import com.application.minipay.domains.User;
import com.application.minipay.enums.Currency;
import com.application.minipay.exceptions.InsufficientBalanceException;
import com.application.minipay.exceptions.UserNotFoundException;
import com.application.minipay.exceptions.ValidationException;
import com.application.minipay.repositories.UserRepository;
import com.application.minipay.services.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletService walletService;

    private static User buildUser(BigDecimal balance) {
        return User.builder()
                .id(UUID.randomUUID())
                .fullName("Test")
                .phone("123456789")
                .balance(balance)
                .currency(Currency.USD)
                .version(0L)
                .createdAt(Instant.now())
                .build();
    }

    // ---- debit ----

    @Test
    void debit_shouldReduceBalance_whenAmountLessThanBalance() {
        User user = buildUser(new BigDecimal("10000"));
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        walletService.debit(user.getId(), new BigDecimal("3000"));

        assertEquals(0, new BigDecimal("7000").compareTo(user.getBalance()));
    }

    @Test
    void debit_shouldSetBalanceToZero_whenAmountEqualsBalance() {
        User user = buildUser(new BigDecimal("5000"));
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        walletService.debit(user.getId(), new BigDecimal("5000"));

        assertEquals(0, BigDecimal.ZERO.compareTo(user.getBalance()));
    }

    @Test
    void debit_shouldThrowInsufficientBalance_whenAmountExceedsBalance() {
        User user = buildUser(new BigDecimal("100"));
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        assertThrows(InsufficientBalanceException.class,
                () -> walletService.debit(user.getId(), new BigDecimal("500")));
    }

    @Test
    void debit_shouldThrowValidationException_whenAmountIsZeroOrNegative() {
        User user = buildUser(new BigDecimal("10000"));
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class,
                () -> walletService.debit(user.getId(), new BigDecimal("-50")));
    }

    @Test
    void debit_shouldThrowUserNotFound_whenUserDoesNotExist() {
        UUID missingUserId = UUID.randomUUID();
        when(userRepository.findByIdForUpdate(missingUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> walletService.debit(missingUserId, new BigDecimal("100")));
    }

    // ---- credit ----

    @Test
    void credit_shouldIncreaseBalance_whenAmountIsPositive() {
        User user = buildUser(new BigDecimal("10000"));
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        walletService.credit(new BigDecimal("5000"), user.getId());

        assertEquals(0, new BigDecimal("15000").compareTo(user.getBalance()));
    }

    @Test
    void credit_shouldThrowValidationException_whenAmountIsZeroOrNegative() {
        User user = buildUser(new BigDecimal("10000"));
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class,
                () -> walletService.credit(new BigDecimal("-50"), user.getId()));
    }

    @Test
    void credit_shouldThrowUserNotFound_whenUserDoesNotExist() {
        UUID missingUserId = UUID.randomUUID();
        when(userRepository.findByIdForUpdate(missingUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> walletService.credit(new BigDecimal("100"), missingUserId));
    }
}