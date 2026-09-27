package com.urlshortener.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.urlshortener.entity.UrlMapping;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

	Optional<UrlMapping> findByShortCode(String shortCode);

	boolean existsByOriginalUrl(String originalUrl);

	@Modifying
	@Query("update UrlMapping m set m.hitCount = m.hitCount + 1 where m.shortCode = :shortCode")
	int incrementHitCount(@Param("shortCode") String shortCode);

}
