package com.deploy.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deploy.post.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
