package com.application.minipay.service;

import com.application.minipay.domains.Provider;
import com.application.minipay.dtos.ProviderRequestDTO;
import com.application.minipay.dtos.ProviderResponseDTO;
import com.application.minipay.exceptions.ProviderAlreadyExistsException;
import com.application.minipay.exceptions.ProviderNotAvailableException;
import com.application.minipay.repositories.ProviderRepository;
import com.application.minipay.services.ProviderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ProviderService providerService;

    private static Provider buildProvider(UUID id, String code) {
        return Provider.builder()
                .id(id)
                .code(code)
                .name("Provider " + code)
                .active(true)
                .build();
    }

    @Test
    void findProvider_shouldReturnDto_whenProviderExists() {
        UUID providerId = UUID.randomUUID();
        Provider provider = buildProvider(providerId, "PROVIDER_A");
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));

        ProviderResponseDTO response = providerService.findProvider(providerId);

        assertEquals(providerId, response.getId());
        assertEquals("PROVIDER_A", response.getCode());
    }

    @Test
    void findProvider_shouldThrowProviderNotAvailable_whenProviderDoesNotExist() {
        UUID providerId = UUID.randomUUID();
        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());

        assertThrows(ProviderNotAvailableException.class, () -> providerService.findProvider(providerId));
    }

    @Test
    void findAllProviders_shouldReturnMappedList() {
        Provider provider1 = buildProvider(UUID.randomUUID(), "PROVIDER_A");
        Provider provider2 = buildProvider(UUID.randomUUID(), "PROVIDER_B");
        when(providerRepository.findAll()).thenReturn(List.of(provider1, provider2));

        List<ProviderResponseDTO> response = providerService.findAllProviders();

        assertEquals(2, response.size());
    }

    @Test
    void createProvider_shouldSaveAndReturnDto_whenCodeNotTaken() {
        ProviderRequestDTO request = ProviderRequestDTO.builder()
                .code("PROVIDER_C")
                .name("Provider C")
                .active(true)
                .build();

        when(providerRepository.findByCode("PROVIDER_C")).thenReturn(Optional.empty());
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProviderResponseDTO response = providerService.createProvider(request);

        assertEquals("PROVIDER_C", response.getCode());
        assertTrue(response.isActive());
    }

    @Test
    void createProvider_shouldThrowProviderAlreadyExists_whenCodeAlreadyTaken() {
        ProviderRequestDTO request = ProviderRequestDTO.builder()
                .code("PROVIDER_A")
                .name("Provider A duplicate")
                .active(true)
                .build();

        when(providerRepository.findByCode("PROVIDER_A"))
                .thenReturn(Optional.of(buildProvider(UUID.randomUUID(), "PROVIDER_A")));

        assertThrows(ProviderAlreadyExistsException.class, () -> providerService.createProvider(request));
        verify(providerRepository, never()).save(any());
    }

    @Test
    void deleteProvider_shouldCallDeleteById() {
        UUID providerId = UUID.randomUUID();

        providerService.deleteProvider(providerId);

        verify(providerRepository, times(1)).deleteById(providerId);
    }
}