package com.deploy;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.deploy.post.service.PostNotFoundException;
import com.deploy.post.service.PostService;

/**
 * DB 없이 웹 계층만 띄워서 검사한다.
 * (빌드 서버/CI에 MySQL이 없어도 ./gradlew build 가 통과하도록)
 */
@WebMvcTest
class ApplicationTests {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	PostService postService;

	@Test
	void health_returnsOk() throws Exception {
		mockMvc.perform(get("/api/health"))
				.andExpect(status().isOk())
				.andExpect(content().string("OK"));
	}

	@Test
	void detail_notFound_returns404() throws Exception {
		given(postService.findById(999L)).willThrow(new PostNotFoundException(999L));

		mockMvc.perform(get("/api/posts/999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void create_invalidBody_returns400() throws Exception {
		mockMvc.perform(post("/api/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"\",\"content\":\"\",\"author\":\"\"}"))
				.andExpect(status().isBadRequest());
	}
}
