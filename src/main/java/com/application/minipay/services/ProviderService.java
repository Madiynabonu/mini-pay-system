package com.application.minipay.services;

import com.application.minipay.domains.Provider;
import com.application.minipay.dtos.ProviderRequestDTO;
import com.application.minipay.dtos.ProviderResponseDTO;
import com.application.minipay.exceptions.ProviderAlreadyExistsException;
import com.application.minipay.exceptions.ProviderNotAvailableException;
import com.application.minipay.repositories.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;


    public List<ProviderResponseDTO> findAllProviders() {


        List<Provider> providers = providerRepository.findAll();

        return providers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());


    }


    public ProviderResponseDTO findProvider(UUID providerId) {


        Optional<Provider> provider = providerRepository.findById(providerId);
        if (provider.isEmpty()) {

            throw new ProviderNotAvailableException(providerId);
        }
        return toDto(provider.get());
    }


    public ProviderResponseDTO createProvider(ProviderRequestDTO providerRequestDTO) {

        if (providerRepository.findByCode(providerRequestDTO.getCode()).isPresent()) {
            throw new ProviderAlreadyExistsException(providerRequestDTO.getCode());
        }
        Provider provider = providerRepository.save(toDto(providerRequestDTO));
        return toDto(provider);


    }


    private ProviderResponseDTO toDto(Provider provider) {


        return ProviderResponseDTO.builder()
                .id(provider.getId())
                .code(provider.getCode())
                .name(provider.getName())
                .active(provider.isActive())
                .build();


    }

    private Provider toDto(ProviderRequestDTO provider) {


        return Provider.builder()
                .id(UUID.randomUUID())
                .code(provider.getCode())
                .name(provider.getName())
                .active(provider.isActive())
                .build();

    }


    public void deleteProvider(UUID providerId) {
        providerRepository.deleteById(providerId);
    }
}
