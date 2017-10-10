package com.sungmook.transaction.repository;

import com.sungmook.transaction.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
