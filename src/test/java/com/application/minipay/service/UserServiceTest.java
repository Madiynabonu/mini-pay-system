package com.application.minipay.service;

import com.application.minipay.domains.User;
import com.application.minipay.dtos.UserRequestDTO;
import com.application.minipay.dtos.UserResponseDTO;
import com.application.minipay.enums.Currency;
import com.application.minipay.exceptions.UserAlreadyExistsException;
import com.application.minipay.exceptions.UserNotFoundException;
import com.application.minipay.repositories.UserRepository;
import com.application.minipay.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private static User buildUser(UUID id, String phone) {
        return User.builder()
                .id(id)
                .fullName("Test User")
                .phone(phone)
                .balance(new BigDecimal("1000"))
                .currency(Currency.UZS)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void findUser_shouldReturnDto_whenUserExists() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "+998900000001");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.findUser(userId);

        assertEquals(userId, response.getId());
        assertEquals("+998900000001", response.getPhone());
    }

    @Test
    void findUser_shouldThrowUserNotFound_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUser(userId));
    }

    @Test
    void findAllUsers_shouldReturnMappedList() {
        User user1 = buildUser(UUID.randomUUID(), "+998900000002");
        User user2 = buildUser(UUID.randomUUID(), "+998900000003");
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDTO> response = userService.findAllUsers();

        assertEquals(2, response.size());
    }

    @Test
    void createUser_shouldSaveAndReturnDto_whenPhoneNotTaken() {
        UserRequestDTO request = UserRequestDTO.builder()
                .fullName("New User")
                .phone("+998900000004")
                .balance(new BigDecimal("500"))
                .currency(Currency.UZS)
                .build();

        when(userRepository.findByPhone("+998900000004")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.createUser(request);

        assertEquals("+998900000004", response.getPhone());
        assertEquals(0, new BigDecimal("500").compareTo(response.getBalance()));
    }

    @Test
    void createUser_shouldThrowUserAlreadyExists_whenPhoneAlreadyTaken() {
        UserRequestDTO request = UserRequestDTO.builder()
                .fullName("Duplicate User")
                .phone("+998900000005")
                .balance(new BigDecimal("500"))
                .currency(Currency.UZS)
                .build();

        when(userRepository.findByPhone("+998900000005"))
                .thenReturn(Optional.of(buildUser(UUID.randomUUID(), "+998900000005")));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldDelete_whenUserExists() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "+998900000006");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_shouldThrowUserNotFound_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).delete(any());
    }
}