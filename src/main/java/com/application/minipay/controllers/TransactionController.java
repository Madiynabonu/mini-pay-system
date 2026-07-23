package com.application.minipay.controllers;

import com.application.minipay.dtos.TransactionRequestDTO;
import com.application.minipay.dtos.TransactionResponseDTO;
import com.application.minipay.enums.Status;
import com.application.minipay.services.TransactionChargeResult;
import com.application.minipay.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
        TransactionChargeResult result = transactionService.charge(transactionRequestDTO, idempotencyKey);
        HttpStatus status = result.isNewTransaction() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.transaction());
    }


    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable UUID transactionId) {

        log.info("REST request to get Transaction : {}", transactionId);
        return ResponseEntity.ok(transactionService.findById(transactionId));
    }


    @GetMapping
    public ResponseEntity<Page<TransactionResponseDTO>> getTransactions(@RequestParam UUID userId, @RequestParam(required = false) Status status, Pageable pageable) {
        log.info("REST request to get Transactions for user : {} , with page : " + userId, pageable);

        return ResponseEntity.ok(transactionService.findTransactionsByUserIdWithPageable(userId, status, pageable));

    }


}