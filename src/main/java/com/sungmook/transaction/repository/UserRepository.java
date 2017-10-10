package com.sungmook.transaction.repository;

import com.sungmook.transaction.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
