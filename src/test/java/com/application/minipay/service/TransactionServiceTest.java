package com.application.minipay.service;

import com.application.minipay.domains.Provider;
import com.application.minipay.domains.User;
import com.application.minipay.dtos.TransactionRequestDTO;
import com.application.minipay.dtos.TransactionResponseDTO;
import com.application.minipay.enums.Currency;
import com.application.minipay.enums.Status;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

}