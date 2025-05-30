package com.service.bank.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDto {
    private UUID id;
    private String sortCode;
    private String name;
    private String country;
    private UUID clientId;
}

