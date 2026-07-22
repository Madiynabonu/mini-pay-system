package com.application.minipay.controllers;

import com.application.minipay.dtos.UserRequestDTO;
import com.application.minipay.dtos.UserResponseDTO;
import com.application.minipay.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.debug("REST request to get all Users");
        return ResponseEntity.ok(userService.findAllUsers());

    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable UUID userId) {
        log.debug("REST request to get User : {}", userId);
        return ResponseEntity.ok(userService.findUser(userId));
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO userRequestDTO) {
        log.debug("REST request to create User : {}", userRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequestDTO));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> createUser(@PathVariable UUID userId) {
        log.debug("REST request to delete User : {}", userId);

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }


}