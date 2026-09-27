package com.urlshortener.rest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.urlshortener.dto.UrlResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.exception.GlobalExceptionHandler;
import com.urlshortener.exception.UrlAlreadyExistsException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.service.UrlShorteningService;

@WebMvcTest(controllers = { UrlController.class, RedirectController.class })
@Import(GlobalExceptionHandler.class)
class UrlControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UrlShorteningService service;

	private UrlResponse sampleResponse() {
		return new UrlResponse(1L, "https://example.com", "abc123", LocalDateTime.now(), LocalDateTime.now());
	}

	@Test
	void createShortUrl_returnsCreated() throws Exception {
		when(service.createShortUrl("https://example.com")).thenReturn(sampleResponse());

		mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
				.content("{\"url\": \"https://example.com\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.url").value("https://example.com"))
				.andExpect(jsonPath("$.shortCode").value("abc123"));
	}

	@Test
	void createShortUrl_rejectsBlankUrl() throws Exception {
		mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content("{\"url\": \"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createShortUrl_rejectsInvalidScheme() throws Exception {
		mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
				.content("{\"url\": \"ftp://example.com\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createShortUrl_returnsConflictWhenAlreadyExists() throws Exception {
		when(service.createShortUrl("https://example.com"))
				.thenThrow(new UrlAlreadyExistsException("URL already exists"));

		mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
				.content("{\"url\": \"https://example.com\"}"))
				.andExpect(status().isConflict());
	}

	@Test
	void getOriginalUrl_returnsUrl() throws Exception {
		when(service.getOriginalUrl("abc123")).thenReturn(sampleResponse());

		mockMvc.perform(get("/shorten/abc123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("https://example.com"));
	}

	@Test
	void getOriginalUrl_returns404WhenMissing() throws Exception {
		when(service.getOriginalUrl("nope")).thenThrow(new UrlNotFoundException("Short URL not found"));

		mockMvc.perform(get("/shorten/nope")).andExpect(status().isNotFound());
	}

	@Test
	void redirect_returnsFoundWithLocation() throws Exception {
		when(service.getOriginalUrl("abc123")).thenReturn(sampleResponse());

		mockMvc.perform(get("/abc123"))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "https://example.com"));
	}

	@Test
	void redirect_returns404WhenMissing() throws Exception {
		when(service.getOriginalUrl("nope")).thenThrow(new UrlNotFoundException("Short URL not found"));

		mockMvc.perform(get("/nope")).andExpect(status().isNotFound());
	}

	@Test
	void updateOriginalUrl_returnsUpdatedUrl() throws Exception {
		UrlResponse updated = new UrlResponse(1L, "https://new.example.com", "abc123",
				LocalDateTime.now(), LocalDateTime.now());
		when(service.updateOriginalUrl(eq("abc123"), eq("https://new.example.com"))).thenReturn(updated);

		mockMvc.perform(put("/shorten/abc123").contentType(MediaType.APPLICATION_JSON)
				.content("{\"url\": \"https://new.example.com\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("https://new.example.com"));
	}

	@Test
	void deleteShortUrl_returnsNoContent() throws Exception {
		mockMvc.perform(delete("/shorten/abc123")).andExpect(status().isNoContent());
	}

	@Test
	void deleteShortUrl_returns404WhenMissing() throws Exception {
		doThrow(new UrlNotFoundException("Short URL not found")).when(service).deleteShortUrl("nope");

		mockMvc.perform(delete("/shorten/nope")).andExpect(status().isNotFound());
	}

	@Test
	void getUrlStats_returnsStats() throws Exception {
		UrlStatsResponse stats = new UrlStatsResponse(1L, "https://example.com", "abc123",
				LocalDateTime.now(), LocalDateTime.now(), 42L);
		when(service.getUrlStats("abc123")).thenReturn(stats);

		mockMvc.perform(get("/shorten/abc123/stats"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessCount").value(42));
	}

	@Test
	void getUrlStats_returns404WhenMissing() throws Exception {
		when(service.getUrlStats("nope")).thenThrow(new UrlNotFoundException("Short URL not found"));

		mockMvc.perform(get("/shorten/nope/stats")).andExpect(status().isNotFound());
	}
}
