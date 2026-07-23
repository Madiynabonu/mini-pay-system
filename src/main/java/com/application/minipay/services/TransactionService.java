package com.application.minipay.services;

import com.application.minipay.domains.Provider;
import com.application.minipay.domains.Transaction;
import com.application.minipay.domains.User;
import com.application.minipay.dtos.TransactionRequestDTO;
import com.application.minipay.dtos.TransactionResponseDTO;
import com.application.minipay.enums.Status;
import com.application.minipay.exceptions.*;
import com.application.minipay.providers.ProviderChargeRequest;
import com.application.minipay.providers.ProviderChargeResult;
import com.application.minipay.repositories.ProviderRepository;
import com.application.minipay.repositories.TransactionRepository;
import com.application.minipay.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final WalletService walletService;
    private final ProviderResolver providerResolver;


    public TransactionChargeResult charge(TransactionRequestDTO request, String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            throw new ValidationException("idempotencyKey cannot be empty");
        }
        if (request.getAmount().compareTo(new BigDecimal(0)) <= 0) {
            throw new ValidationException("Amount must be greater than 0");
        }

        Optional<Provider> provider = providerRepository.findById(request.getProviderId());

        if (provider.isEmpty() || !provider.get().isActive()) {
            throw new ProviderNotAvailableException(request.getProviderId());
        }

        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            throw new UserNotFoundException(request.getUserId());
        }

        Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            return new TransactionChargeResult(toResponseDTO(existingTransaction.get()), false);
        }

        walletService.debit(user.get().getId(), request.getAmount());

        Transaction transaction = Transaction.builder()
                .user(user.get())
                .provider(provider.get())
                .serviceAccount(request.getServiceAccount())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(Status.PENDING)
                .idempotencyKey(idempotencyKey)
                .createdAt(Instant.now())
                .build();

        transactionRepository.save(transaction);


        try {

            ProviderChargeResult result = providerResolver.resolve(provider.get().getCode()).charge(new ProviderChargeRequest(request.getServiceAccount(), request.getAmount(), request.getCurrency()));

            if (result.status() == Status.SUCCESS) {
                transaction.setStatus(Status.SUCCESS);
                transaction.setProviderRef(result.providerRef());
            } else {
                transaction.setStatus(Status.FAILED);
                walletService.credit(request.getAmount(), request.getUserId());
                transaction.setFailureReason(result.failureReason());
            }

        } catch (ProviderCommunicationException exception) {
            transaction.setStatus(Status.FAILED);
            walletService.credit(request.getAmount(), request.getUserId());
            transaction.setFailureReason("PROVIDER_COMMUNICATION_ERROR");
            transactionRepository.save(transaction);

        }


        transactionRepository.save(transaction);
        return new TransactionChargeResult(toResponseDTO(transaction), true);

    }

    public static TransactionResponseDTO toResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getId())
                .userId(transaction.getUser().getId())
                .providerId(transaction.getProvider().getId())
                .serviceAccount(transaction.getServiceAccount())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public static TransactionResponseDTO toResponseDTO(TransactionRequestDTO transaction) {
        return TransactionResponseDTO.builder()
                .userId(transaction.getUserId())
                .providerId(transaction.getProviderId())
                .serviceAccount(transaction.getServiceAccount())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .build();
    }

    public TransactionResponseDTO findById(UUID transactionId) {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isEmpty()) {
            throw new TransactionNotFoundException(transactionId);
        }
        return toResponseDTO(transaction.get());
    }

    public Page<TransactionResponseDTO> findTransactionsByUserIdWithPageable(UUID userId, Status status, Pageable pageable) {
        Page<Transaction> transactions = status == null
                ? transactionRepository.findAllByUserId(userId, pageable)
                : transactionRepository.findAllByUserIdAndStatus(userId, status, pageable);
        return transactions.map(TransactionService::toResponseDTO);
    }
}
