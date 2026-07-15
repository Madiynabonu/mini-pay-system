package com.application.minipay.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@Builder
public class ProviderResponseDTO {


    private UUID id;

    private String code;

    private String name;

    private boolean active;


}
