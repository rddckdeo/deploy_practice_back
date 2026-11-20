package com.deploy.post.dto;

import java.time.LocalDateTime;

import com.deploy.post.domain.Post;

public record PostResponse(
		Long id,
		String title,
		String content,
		String author,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public static PostResponse from(Post post) {
		return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getAuthor(),
				post.getCreatedAt(), post.getUpdatedAt());
	}
}
