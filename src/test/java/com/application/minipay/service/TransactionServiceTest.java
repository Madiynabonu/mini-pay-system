package com.application.minipay.service;

import com.application.minipay.domains.Provider;
import com.application.minipay.domains.Transaction;
import com.application.minipay.domains.User;
import com.application.minipay.dtos.TransactionRequestDTO;
import com.application.minipay.dtos.TransactionResponseDTO;
import com.application.minipay.enums.Currency;
import com.application.minipay.enums.Status;
import com.application.minipay.exceptions.ProviderCommunicationException;
import com.application.minipay.exceptions.ProviderNotAvailableException;
import com.application.minipay.exceptions.TransactionNotFoundException;
import com.application.minipay.exceptions.UserNotFoundException;
import com.application.minipay.exceptions.ValidationException;
import com.application.minipay.providers.ProviderChargeResult;
import com.application.minipay.providers.ProviderPlugin;
import com.application.minipay.repositories.ProviderRepository;
import com.application.minipay.repositories.TransactionRepository;
import com.application.minipay.repositories.UserRepository;
import com.application.minipay.services.ProviderResolver;
import com.application.minipay.services.TransactionChargeResult;
import com.application.minipay.services.TransactionService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private ProviderRepository providerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProviderResolver providerResolver;
    @Mock
    private WalletService walletService;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    void charge_shouldReturnSuccess_whenProviderApproves() {
        UUID userId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();

        User user = User.builder().id(userId).balance(new BigDecimal("10000")).currency(Currency.UZS).build();
        Provider provider = Provider.builder().id(providerId).code("PROVIDER_A").active(true).build();

        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .userId(userId).providerId(providerId)
                .serviceAccount("+998901234567")
                .amount(new BigDecimal("5000"))
                .currency(Currency.UZS)
                .build();

        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());

        ProviderPlugin mockPlugin = mock(ProviderPlugin.class);
        when(providerResolver.resolve("PROVIDER_A")).thenReturn(mockPlugin);
        when(mockPlugin.charge(any())).thenReturn(new ProviderChargeResult(Status.SUCCESS, "ref-123", null));

        TransactionChargeResult result = transactionService.charge(request, "key-1");
        TransactionResponseDTO response = result.transaction();

        assertEquals(Status.SUCCESS, response.getStatus());
        assertTrue(result.isNewTransaction());
        verify(walletService, never()).credit(any(), any());
    }

    @Test
    void charge_shouldReleaseHoldAndReturnFailed_whenProviderDeclines() {
        UUID userId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();

        User user = User.builder().id(userId).balance(new BigDecimal("10000")).currency(Currency.UZS).build();
        Provider provider = Provider.builder().id(providerId).code("PROVIDER_A").active(true).build();

        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .userId(userId).providerId(providerId)
                .serviceAccount("+998901234567")
                .amount(new BigDecimal("5000"))
                .currency(Currency.UZS)
                .build();

        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());

        ProviderPlugin mockPlugin = mock(ProviderPlugin.class);
        when(providerResolver.resolve("PROVIDER_A")).thenReturn(mockPlugin);
        when(mockPlugin.charge(any())).thenReturn(new ProviderChargeResult(Status.FAILED, null, "PROVIDER_DECLINED"));

        TransactionChargeResult result = transactionService.charge(request, "key-2");
        TransactionResponseDTO response = result.transaction();

        assertEquals(Status.FAILED, response.getStatus());
        assertEquals("PROVIDER_DECLINED", response.getFailureReason());
        assertTrue(result.isNewTransaction());
        verify(walletService, times(1)).credit(new BigDecimal("5000"), userId);
    }

    @Test
    void charge_shouldThrowValidationException_whenIdempotencyKeyIsEmpty() {
        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .userId(UUID.randomUUID()).providerId(UUID.randomUUID())
                .serviceAccount("+998901234567")
                .amount(new BigDecimal("5000"))
                .currency(Currency.UZS)
                .build();

        assertThrows(ValidationException.class, () -> transactionService.charge(request, ""));
        verify(walletService, never()).debit(any(), any());
    }

    @Test
    void charge_shouldThrowValidationException_whenAmountIsZeroOrNegative() {
        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .userId(UUID.randomUUID()).providerId(UUID.randomUUID())
                .serviceAccount("+998901234567")
                .amount(new BigDecimal("-100"))
                .currency(Currency.UZS)
                .build();

        assertThrows(ValidationException.class, () -> transactionService.charge(request, "key-3"));
        verify(walletService, never()).debit(any(), any());
    }

    @Test
    void charge_shouldThrowProviderNotAvailable_whenProviderDoesNotExist() {
        UUID providerId = UUID.randomUUID();
        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .userId(UUID.randomUUID()).providerId(providerId)
                .serviceAccount("+998901234567")
                .amount(new BigDecimal("5000"))
                .currency(Currency.UZS)
                .build();

        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());

        assertThrows(ProviderNotAvailableException.class, () -> transactionService.charge(request, "key-4"));
        verify(walletService, never()).debit(any(), any());
    }

    @Test
    void charge_shouldThrowUserNotFound_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        Provider provider = Provider.builder().id(providerId).code("PROVIDER_A").active(true).build();

        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .userId(userId).providerId(providerId)
                .serviceAccount("+998901234567")
                .amount(new BigDecimal("5000"))
                .currency(Currency.UZS)
                .build();

        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> transactionService.charge(request, "key-5"));
        verify(walletService, never()).debit(any(), any());
    }

    @Test
    void charge_shouldReturnExistingTransaction_whenIdempotencyKeyAlreadyUsed() {
        UUID userId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        User user = User.builder().id(userId).balance(new BigDecimal("10000")).currency(Currency.UZS).build();
        Provider provider = Provider.builder().id(providerId).code("PROVIDER_A").active(true).build();

        Transaction existing = Transaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .provider(provider)
                .serviceAccount("+998901234567")
                .amount(new BigDecimal("5000"))
                .currency(Currency.UZS)
                .status(Status.SUCCESS)
                .idempotencyKey("key-6")
                .createdAt(Instant.now())
                .build();

        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .userId(userId).providerId(providerId)
                .serviceAccount("+998901234567")
                .amount(new BigDecimal("5000"))
                .currency(Currency.UZS)
                .build();

        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdempotencyKey("key-6")).thenReturn(Optional.of(existing));

        TransactionChargeResult result = transactionService.charge(request, "key-6");

        assertEquals(existing.getId(), result.transaction().getTransactionId());
        assertFalse(result.isNewTransaction());
        verify(walletService, never()).debit(any(), any());
    }

    @Test
    void charge_shouldReleaseHoldAndReturnFailed_whenProviderCommunicationFails() {
        UUID userId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();

        User user = User.builder().id(userId).balance(new BigDecimal("10000")).currency(Currency.UZS).build();
        Provider provider = Provider.builder().id(providerId).code("PROVIDER_A").active(true).build();

        TransactionRequestDTO request = TransactionRequestDTO.builder()
                .userId(userId).providerId(providerId)
                .serviceAccount("+998901234567")
                .amount(new BigDecimal("5000"))
                .currency(Currency.UZS)
                .build();

        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.findByIdempotencyKey("key-7")).thenReturn(Optional.empty());

        ProviderPlugin mockPlugin = mock(ProviderPlugin.class);
        when(providerResolver.resolve("PROVIDER_A")).thenReturn(mockPlugin);
        when(mockPlugin.charge(any())).thenThrow(new ProviderCommunicationException("timeout", new RuntimeException()));

        TransactionChargeResult result = transactionService.charge(request, "key-7");

        assertEquals(Status.FAILED, result.transaction().getStatus());
        assertEquals("PROVIDER_COMMUNICATION_ERROR", result.transaction().getFailureReason());
        verify(walletService, times(1)).credit(new BigDecimal("5000"), userId);
    }

    @Test
    void findById_shouldThrowTransactionNotFound_whenTransactionDoesNotExist() {
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> transactionService.findById(transactionId));
    }

}