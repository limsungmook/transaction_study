package com.sungmook.transaction;

import com.sungmook.transaction.model.User;
import lombok.Data;

@Data
public class TransactionCommittedEvent {

    private User user;
    private boolean firePostException;

    public TransactionCommittedEvent(User user, boolean firePostException) {
        this.user = user;
        this.firePostException = firePostException;
    }
}
