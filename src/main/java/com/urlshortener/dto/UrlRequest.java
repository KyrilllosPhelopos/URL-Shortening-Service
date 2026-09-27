package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UrlRequest {

	@NotBlank(message = "url must not be blank")
	@Size(max = 2048, message = "url must be at most 2048 characters")
	@Pattern(regexp = "^https?://\\S+$", message = "url must be a valid http(s) URL")
	private String url;
}
