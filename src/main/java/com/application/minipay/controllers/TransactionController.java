package com.application.minipay.controllers;

import com.application.minipay.dtos.TransactionRequestDTO;
import com.application.minipay.dtos.TransactionResponseDTO;
import com.application.minipay.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@RequestHeader("Idempotency-Key") String idempotencyKey, @RequestBody TransactionRequestDTO transactionRequestDTO) {

        log.info("REST request to create Transaction : {}", transactionRequestDTO);
        return ResponseEntity.ok(transactionService.charge(transactionRequestDTO, idempotencyKey));
    }


    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable UUID transactionId) {

        log.info("REST request to get Transaction : {}", transactionId);
        return ResponseEntity.ok(transactionService.findById(transactionId));
    }


}