package com.application.minipay.services;

import com.application.minipay.domains.User;
import com.application.minipay.dtos.UserRequestDTO;
import com.application.minipay.dtos.UserResponseDTO;
import com.application.minipay.exceptions.UserAlreadyExistsException;
import com.application.minipay.exceptions.UserNotFoundException;
import com.application.minipay.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponseDTO findUser(UUID userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new UserNotFoundException(userId);

        }
        return toDto(user.get());
    }

    public List<UserResponseDTO> findAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());


    }

    public UserResponseDTO createUser(UserRequestDTO userRequest) {

        if (userRepository.findByPhone(userRequest.getPhone()).isPresent()) {
            throw new UserAlreadyExistsException(userRequest.getPhone());
        }

        return toDto(userRepository.save(
                User.builder()
                        .id(UUID.randomUUID())
                        .fullName(userRequest.getFullName())
                        .phone(userRequest.getPhone())
                        .balance(userRequest.getBalance())
                        .currency(userRequest.getCurrency())
                        .createdAt(Instant.now())
                        .build())
        );


    }

    public void deleteUser(UUID userId) {

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new UserNotFoundException(userId);
        }
        userRepository.delete(user.get());
    }

    public UserResponseDTO toDto(User user) {


        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .balance(user.getBalance())
                .currency(user.getCurrency())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }


}
