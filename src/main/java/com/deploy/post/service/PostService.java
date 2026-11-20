package com.deploy.post.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deploy.post.domain.Post;
import com.deploy.post.dto.PostCreateRequest;
import com.deploy.post.dto.PostResponse;
import com.deploy.post.dto.PostUpdateRequest;
import com.deploy.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private final PostRepository postRepository;

	public List<PostResponse> findAll() {
		return postRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
				.map(PostResponse::from)
				.toList();
	}

	public PostResponse findById(Long id) {
		return PostResponse.from(getPost(id));
	}

	@Transactional
	public PostResponse create(PostCreateRequest request) {
		Post post = Post.builder()
				.title(request.title())
				.content(request.content())
				.author(request.author())
				.build();
		return PostResponse.from(postRepository.save(post));
	}

	@Transactional
	public PostResponse update(Long id, PostUpdateRequest request) {
		Post post = getPost(id);
		post.update(request.title(), request.content());
		postRepository.flush(); // updatedAt(@PreUpdate)이 응답에 반영되도록 즉시 반영
		return PostResponse.from(post);
	}

	@Transactional
	public void delete(Long id) {
		postRepository.delete(getPost(id));
	}

	private Post getPost(Long id) {
		return postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
	}
}
