package com.urlshortener.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlStatsResponse {
	private long id;
	private String url;
	private String shortCode;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private long accessCount;
}
