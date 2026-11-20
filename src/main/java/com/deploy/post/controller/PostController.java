package com.deploy.post.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deploy.post.dto.PostCreateRequest;
import com.deploy.post.dto.PostResponse;
import com.deploy.post.dto.PostUpdateRequest;
import com.deploy.post.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@GetMapping
	public List<PostResponse> list() {
		return postService.findAll();
	}

	@GetMapping("/{id}")
	public PostResponse detail(@PathVariable("id") Long id) {
		return postService.findById(id);
	}

	@PostMapping
	public ResponseEntity<PostResponse> create(@Validated @RequestBody PostCreateRequest request) {
		PostResponse created = postService.create(request);
		return ResponseEntity.created(URI.create("/api/posts/" + created.id())).body(created);
	}

	@PutMapping("/{id}")
	public PostResponse update(@PathVariable("id") Long id, @Validated @RequestBody PostUpdateRequest request) {
		return postService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
		postService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
