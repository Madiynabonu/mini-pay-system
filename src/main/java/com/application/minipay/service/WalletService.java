package com.application.minipay.service;

import com.application.minipay.domains.User;
import com.application.minipay.exceptions.InsufficientBalanceException;
import com.application.minipay.exceptions.UserNotFoundException;
import com.application.minipay.exceptions.ValidationException;
import com.application.minipay.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class WalletService {

    private final UserRepository userRepository;

    @Transactional
    public void debit(UUID userId, BigDecimal amount) {


        User user = userRepository.findByIdForUpdate(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (amount.compareTo(new BigDecimal(0)) <= 0) {
            throw new ValidationException("No debit");
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }

        BigDecimal balance = user.getBalance();

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(userId, balance);
        } else if (amount.compareTo(balance) == 0) {
            user.setBalance(new BigDecimal(0));
        } else if (balance.compareTo(amount) > 0) {
            user.setBalance(balance.subtract(amount));
        }
        userRepository.save(user);

    }

    @Transactional
    public void credit(BigDecimal amount, UUID userId) {

        User user = userRepository.findByIdForUpdate(userId).orElseThrow(() -> new UserNotFoundException(userId));
        BigDecimal balance = user.getBalance();

        if (amount.compareTo(new BigDecimal(0)) <= 0) {
            throw new ValidationException("Amount must be positive");
        }
        user.setBalance(balance.add(amount));
        userRepository.save(user);

    }
}
