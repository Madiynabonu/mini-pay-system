package com.application.minipay.service;

import com.application.minipay.domains.User;
import com.application.minipay.enums.Currency;
import com.application.minipay.exceptions.InsufficientBalanceException;
import com.application.minipay.repositories.UserRepository;
import com.application.minipay.services.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class WalletServiceConcurrencyTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void debit_shouldAllowOnlyOneOfTwoConcurrentRequests_whenBalanceInsufficientForBoth() throws InterruptedException {
        User savedUser = userRepository.save(User.builder()
                .fullName("Concurrency Test User")
                .phone("+998900000000_" + UUID.randomUUID())
                .balance(new BigDecimal("1000"))
                .currency(Currency.UZS)
                .createdAt(Instant.now())
                .build());

        UUID userId = savedUser.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Callable<String> debitTask = () -> {
            startLatch.await();
            walletService.debit(userId, new BigDecimal("700"));
            return "SUCCESS";
        };

        List<Future<String>> futures = List.of(
                executor.submit(debitTask),
                executor.submit(debitTask)
        );

        startLatch.countDown();

        int successCount = 0;
        int insufficientBalanceCount = 0;
        for (Future<String> future : futures) {
            try {
                future.get();
                successCount++;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof InsufficientBalanceException) {
                    insufficientBalanceCount++;
                } else {
                    throw new RuntimeException(e.getCause());
                }
            }
        }

        executor.shutdown();

        assertEquals(1, successCount);
        assertEquals(1, insufficientBalanceCount);

        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals(0, new BigDecimal("300").compareTo(updatedUser.getBalance()));
    }
}