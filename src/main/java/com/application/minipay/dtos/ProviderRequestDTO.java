package com.application.minipay.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class ProviderRequestDTO {

    private String code;

    private String name;

    private boolean active;


}
