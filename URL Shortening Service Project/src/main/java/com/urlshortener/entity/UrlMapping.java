package com.urlshortener.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class UrlMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false, length = 2048)
	private String originalUrl;

	@Column(nullable = false, unique = true, length = 10)
	private String shortCode;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	private long hitCount = 0;

	public void updateTime() {
		this.updatedAt = LocalDateTime.now();
	}

	public UrlMapping(String originalUrl, String shortCode) {
		super();
		this.originalUrl = originalUrl;
		this.shortCode = shortCode;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public UrlMapping() {
		super();
	}

}
