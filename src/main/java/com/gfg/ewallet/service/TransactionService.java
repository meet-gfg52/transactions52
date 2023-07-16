package com.gfg.ewallet.service;

import com.gfg.ewallet.domain.Transaction;
import com.gfg.ewallet.service.resource.TransactionMessage;

public interface TransactionService {

    void createTransaction(Transaction transaction);

    void updateTransaction(TransactionMessage message);
}
