package com.gfg.ewallet.service.resource;

import com.gfg.ewallet.domain.Transaction;
import lombok.*;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionRequest {

    private Long senderId;
    private Long receiverId;
    private Double amount;
    private String description;


    public Transaction toTransaction(){
        return Transaction.builder().senderId(senderId)
                .receiverId(receiverId)
                .amount(amount)
                .transactionId(UUID.randomUUID().toString()).build();
    }
}
