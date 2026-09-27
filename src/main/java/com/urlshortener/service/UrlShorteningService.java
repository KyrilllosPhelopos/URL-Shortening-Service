package com.urlshortener.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urlshortener.dao.UrlMappingRepository;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.exception.UrlAlreadyExistsException;
import com.urlshortener.exception.UrlNotFoundException;

@Service
public class UrlShorteningService {

	private final UrlMappingRepository repo;

	private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int SHORT_CODE_LENGTH = 6;
	private static final int MAX_GENERATION_ATTEMPTS = 5;

	public UrlShorteningService(UrlMappingRepository repo) {
		this.repo = repo;
	}

	@Transactional(noRollbackFor = DataIntegrityViolationException.class)
	public UrlResponse createShortUrl(String originalUrl) {
		if (repo.existsByOriginalUrl(originalUrl)) {
			throw new UrlAlreadyExistsException("URL already exists");
		}

		DataIntegrityViolationException lastCollision = null;
		for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {
			try {
				UrlMapping mapping = new UrlMapping(originalUrl, generateShortCode());
				repo.save(mapping);
				return toResponse(mapping);
			} catch (DataIntegrityViolationException e) {
				lastCollision = e;
			}
		}
		throw lastCollision;
	}

	@Transactional
	public UrlResponse getOriginalUrl(String shortCode) {
		UrlMapping entity = findOrThrow(shortCode);
		repo.incrementHitCount(shortCode);
		return toResponse(entity);
	}

	@Transactional
	public UrlResponse updateOriginalUrl(String shortCode, String newUrl) {
		UrlMapping entity = findOrThrow(shortCode);
		entity.setOriginalUrl(newUrl);
		entity.updateTime();
		return toResponse(entity);
	}

	@Transactional
	public void deleteShortUrl(String shortCode) {
		UrlMapping entity = findOrThrow(shortCode);
		repo.delete(entity);
	}

	@Transactional(readOnly = true)
	public UrlStatsResponse getUrlStats(String shortCode) {
		UrlMapping entity = findOrThrow(shortCode);
		return new UrlStatsResponse(entity.getId(), entity.getOriginalUrl(), entity.getShortCode(),
				entity.getCreatedAt(), entity.getUpdatedAt(), entity.getHitCount());
	}

	private UrlMapping findOrThrow(String shortCode) {
		return repo.findByShortCode(shortCode)
				.orElseThrow(() -> new UrlNotFoundException("Short URL not found"));
	}

	private UrlResponse toResponse(UrlMapping mapping) {
		return new UrlResponse(mapping.getId(), mapping.getOriginalUrl(), mapping.getShortCode(),
				mapping.getCreatedAt(), mapping.getUpdatedAt());
	}

	private String generateShortCode() {
		StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
		for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
			sb.append(CHARACTERS.charAt(ThreadLocalRandom.current().nextInt(CHARACTERS.length())));
		}
		return sb.toString();
	}

}
