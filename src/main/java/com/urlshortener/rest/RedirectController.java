package com.urlshortener.rest;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.dto.UrlResponse;
import com.urlshortener.service.UrlShorteningService;

@RestController
public class RedirectController {

	private final UrlShorteningService service;

	public RedirectController(UrlShorteningService service) {
		this.service = service;
	}

	@GetMapping("/{shortCode}")
	public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
		UrlResponse response = service.getOriginalUrl(shortCode);
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(URI.create(response.getUrl()))
				.build();
	}
}
