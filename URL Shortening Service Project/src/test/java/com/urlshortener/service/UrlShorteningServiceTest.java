package com.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.urlshortener.dao.UrlMappingRepository;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.exception.UrlAlreadyExistsException;
import com.urlshortener.exception.UrlNotFoundException;

@ExtendWith(MockitoExtension.class)
class UrlShorteningServiceTest {

	@Mock
	private UrlMappingRepository repo;

	@InjectMocks
	private UrlShorteningService service;

	private UrlMapping sampleEntity() {
		return new UrlMapping("https://example.com", "abc123");
	}

	@Test
	void createShortUrl_savesNewMapping() {
		when(repo.existsByOriginalUrl("https://example.com")).thenReturn(false);
		when(repo.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UrlResponse response = service.createShortUrl("https://example.com");

		assertThat(response.getUrl()).isEqualTo("https://example.com");
		assertThat(response.getShortCode()).hasSize(6);
		verify(repo).save(any(UrlMapping.class));
	}

	@Test
	void createShortUrl_throwsWhenUrlAlreadyExists() {
		when(repo.existsByOriginalUrl("https://example.com")).thenReturn(true);

		assertThatThrownBy(() -> service.createShortUrl("https://example.com"))
				.isInstanceOf(UrlAlreadyExistsException.class);
		verify(repo, never()).save(any(UrlMapping.class));
	}

	@Test
	void createShortUrl_retriesOnShortCodeCollision() {
		when(repo.existsByOriginalUrl("https://example.com")).thenReturn(false);
		when(repo.save(any(UrlMapping.class)))
				.thenThrow(new DataIntegrityViolationException("duplicate short code"))
				.thenAnswer(invocation -> invocation.getArgument(0));

		UrlResponse response = service.createShortUrl("https://example.com");

		assertThat(response).isNotNull();
		verify(repo, times(2)).save(any(UrlMapping.class));
	}

	@Test
	void getOriginalUrl_returnsUrlAndIncrementsHitCount() {
		when(repo.findByShortCode("abc123")).thenReturn(Optional.of(sampleEntity()));

		UrlResponse response = service.getOriginalUrl("abc123");

		assertThat(response.getUrl()).isEqualTo("https://example.com");
		verify(repo).incrementHitCount("abc123");
	}

	@Test
	void getOriginalUrl_throwsWhenNotFound() {
		when(repo.findByShortCode("nope")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getOriginalUrl("nope"))
				.isInstanceOf(UrlNotFoundException.class);
	}

	@Test
	void updateOriginalUrl_updatesUrl() {
		when(repo.findByShortCode("abc123")).thenReturn(Optional.of(sampleEntity()));

		UrlResponse response = service.updateOriginalUrl("abc123", "https://new.example.com");

		assertThat(response.getUrl()).isEqualTo("https://new.example.com");
	}

	@Test
	void updateOriginalUrl_throwsWhenNotFound() {
		when(repo.findByShortCode("nope")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateOriginalUrl("nope", "https://new.example.com"))
				.isInstanceOf(UrlNotFoundException.class);
	}

	@Test
	void deleteShortUrl_removesExistingMapping() {
		when(repo.findByShortCode("abc123")).thenReturn(Optional.of(sampleEntity()));

		service.deleteShortUrl("abc123");

		verify(repo).delete(any(UrlMapping.class));
	}

	@Test
	void deleteShortUrl_throwsWhenNotFound() {
		when(repo.findByShortCode("nope")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.deleteShortUrl("nope"))
				.isInstanceOf(UrlNotFoundException.class);
		verify(repo, never()).delete(any(UrlMapping.class));
	}

	@Test
	void getUrlStats_returnsStats() {
		when(repo.findByShortCode("abc123")).thenReturn(Optional.of(sampleEntity()));

		UrlStatsResponse stats = service.getUrlStats("abc123");

		assertThat(stats.getShortCode()).isEqualTo("abc123");
		assertThat(stats.getAccessCount()).isZero();
	}

	@Test
	void getUrlStats_throwsWhenNotFound() {
		when(repo.findByShortCode("nope")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getUrlStats("nope"))
				.isInstanceOf(UrlNotFoundException.class);
	}
}
