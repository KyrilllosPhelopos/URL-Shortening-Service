package com.urlshortener.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.dto.UrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.service.UrlShorteningService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/shorten")
public class UrlController {

	private final UrlShorteningService service;

	public UrlController(UrlShorteningService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody UrlRequest request) {
		UrlResponse response = service.createShortUrl(request.getUrl());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{shortCode}")
	public UrlResponse getOriginalUrl(@PathVariable String shortCode) {
		return service.getOriginalUrl(shortCode);
	}

	@PutMapping("/{shortCode}")
	public UrlResponse updateOriginalUrl(@PathVariable String shortCode, @Valid @RequestBody UrlRequest request) {
		return service.updateOriginalUrl(shortCode, request.getUrl());
	}

	@DeleteMapping("/{shortCode}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUrl(@PathVariable String shortCode) {
		service.deleteShortUrl(shortCode);
	}

	@GetMapping("/{shortCode}/stats")
	public UrlStatsResponse getUrlStats(@PathVariable String shortCode) {
		return service.getUrlStats(shortCode);
	}
}
