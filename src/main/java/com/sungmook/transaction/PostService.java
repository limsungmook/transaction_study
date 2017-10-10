package com.sungmook.transaction;

import com.sungmook.transaction.model.User;

public interface PostService {
    void save(User user, boolean firePostException);

}
