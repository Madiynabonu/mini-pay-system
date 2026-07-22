package com.application.minipay.controllers;

import com.application.minipay.dtos.ProviderRequestDTO;
import com.application.minipay.dtos.ProviderResponseDTO;
import com.application.minipay.services.ProviderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProviderController {
    private final Logger log = LoggerFactory.getLogger(ProviderController.class);
    private final ProviderService providerService;

    @GetMapping
    public ResponseEntity<List<ProviderResponseDTO>> getAllProviders() {
        log.debug("REST request to get all Providers");
        return ResponseEntity.ok(providerService.findAllProviders());

    }

    @GetMapping("/{providerId}")
    public ResponseEntity<ProviderResponseDTO> getProvider(@PathVariable UUID providerId) {
        log.debug("REST request to get Provider : {}", providerId);
        return ResponseEntity.ok(providerService.findProvider(providerId));
    }

    @PostMapping
    public ResponseEntity<ProviderResponseDTO> createProvider(@RequestBody ProviderRequestDTO providerRequestDTO) {
        log.debug("REST request to create Provider : {}", providerRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(providerService.createProvider(providerRequestDTO));
    }

    @DeleteMapping("/{providerId}")
    public ResponseEntity<Void> createProvider(@PathVariable UUID providerId) {
        log.debug("REST request to delete Provider : {}", providerId);

        providerService.deleteProvider(providerId);
        return ResponseEntity.noContent().build();
    }


}