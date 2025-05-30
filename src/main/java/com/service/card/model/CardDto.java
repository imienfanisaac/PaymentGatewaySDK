package com.service.card.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private UUID id;
    private String accountNo;
    private String holdersName;
    private String cardType;
    private String cardNo;
    private String expiry;
    private String cvv;
    private UUID clientId;
}