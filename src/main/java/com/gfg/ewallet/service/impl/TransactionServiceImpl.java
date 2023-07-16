package com.gfg.ewallet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfg.ewallet.domain.Transaction;
import com.gfg.ewallet.domain.TransactionStatus;
import com.gfg.ewallet.repository.TransactionRepository;
import com.gfg.ewallet.service.resource.NotificationMessage;
import com.gfg.ewallet.service.resource.TransactionMessage;
import com.gfg.ewallet.service.TransactionService;
import com.gfg.ewallet.service.resource.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    TransactionRepository repository;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private final String TRANSACTION_TOPIC="TRANS_CREATED";

    private final String NOTIFICATION_TOPIC="NOTIFICATION";

    private ObjectMapper mapper=new ObjectMapper();

    private String userUri="http://localhost:8001/user/";

    @Override
    public void createTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.PENDING);
        repository.save(transaction);
        TransactionMessage message=new TransactionMessage(transaction.getSenderId(), transaction.getReceiverId(), transaction.getAmount(), transaction.getStatus().toString(), transaction.getTransactionId());
        try {
            kafkaTemplate.send(TRANSACTION_TOPIC,mapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void updateTransaction(TransactionMessage  message) {
        Optional<Transaction> transactionOptional=repository.findByTransactionId(message.getTransactionId());
        if(transactionOptional.isEmpty())
            throw new NullPointerException("transaction not found with id");
        Transaction transaction=transactionOptional.get();
        transaction.setStatus(TransactionStatus.valueOf(message.getStatus()));
        repository.save(transaction);

        if(transaction.getStatus().equals(TransactionStatus.FAILURE)){
            UserResponse senderUserResponse=restTemplate.getForObject(userUri+transaction.getSenderId(),UserResponse.class);
            String subject="Transaction update!";
            StringBuilder builder=new StringBuilder();
            builder.append("Hi ");
            builder.append(senderUserResponse.getUserName());
            builder.append(",\n");
            builder.append("your transaction of Rs.");
            builder.append(transaction.getAmount());
            builder.append(" has been failed. Please try transacting again.");

            String body=builder.toString();
            NotificationMessage senderMessage=new NotificationMessage(senderUserResponse.getEmail(),subject,body);
            try {
                kafkaTemplate.send(NOTIFICATION_TOPIC,mapper.writeValueAsString(senderMessage));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }else{
            UserResponse senderUserResponse=restTemplate.getForObject(userUri+transaction.getSenderId(),UserResponse.class);
            UserResponse receiverUserResponse=restTemplate.getForObject(userUri+transaction.getReceiverId(),UserResponse.class);
            String subject="Transaction update!";
            StringBuilder builder=new StringBuilder();
            builder.append("Hi ");
            builder.append(senderUserResponse.getUserName());
            builder.append(",\n");
            builder.append("your transaction of Rs.");
            builder.append(transaction.getAmount());
            builder.append(" has been processed.");
            String body=builder.toString();
            builder=new StringBuilder();
            builder.append("Hi ");
            builder.append(receiverUserResponse.getUserName());
            builder.append(",\n");
            builder.append("your wallet has been credited with Rs. ");
            builder.append(transaction.getAmount());
            NotificationMessage senderMessage=new NotificationMessage(senderUserResponse.getEmail(),subject,body);
            NotificationMessage receiverMessage=new NotificationMessage(receiverUserResponse.getEmail(),subject,builder.toString());
            try {
                kafkaTemplate.send(NOTIFICATION_TOPIC,mapper.writeValueAsString(senderMessage));
                kafkaTemplate.send(NOTIFICATION_TOPIC,mapper.writeValueAsString(receiverMessage));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }

    }
}
