# URL Shortening Service

A REST API that shortens long URLs into short, unique codes, tracks how often each link is accessed, and redirects visitors back to the original page.

Inspired by the [Roadmap.sh URL Shortening Service project](https://roadmap.sh/projects/url-shortening-service).

## Features

- **Shorten URLs** — Generate a unique 6-character code for any `http(s)` URL
- **Redirect** — Visit the short link and get a `302 Found` redirect to the original URL
- **Hit tracking** — Count how many times each short link is accessed
- **Full CRUD** — Create, read, update, and delete URL mappings
- **Input validation** — Rejects blank, oversized, or non-`http(s)` URLs
- **Idempotency-safe generation** — Retries on short-code collisions and rejects duplicate original URLs with a `409 Conflict`
- **Consistent error responses** — Global exception handler returns a uniform JSON error body

## Tech Stack

| Layer      | Technology                        |
|------------|-----------------------------------|
| Language   | Java 21                           |
| Framework  | Spring Boot 3.3.5                 |
| Web        | Spring Web (REST)                 |
| Persistence| Spring Data JPA + Hibernate       |
| Database   | PostgreSQL (Neon)                 |
| Validation | Bean Validation (Jakarta)         |
| Build      | Maven (Maven Wrapper)             |
| Testing    | JUnit 5, Mockito, AssertJ         |
| Boilerplate| Lombok                            |

## Project Structure

```
src/main/java/com/urlshortener
├── UrlShortenerApplication.java   # Application entry point
├── rest                           # REST controllers
│   ├── UrlController.java         # /shorten CRUD + stats endpoints
│   └── RedirectController.java    # /{shortCode} redirect endpoint
├── service
│   └── UrlShorteningService.java  # Business logic & transactions
├── dao
│   └── UrlMappingRepository.java  # Spring Data JPA repository
├── entity
│   └── UrlMapping.java            # JPA entity (url_mapping table)
├── dto
│   ├── UrlRequest.java            # Request body (validated)
│   ├── UrlResponse.java           # Standard response body
│   └── UrlStatsResponse.java      # Stats response body
└── exception
    ├── GlobalExceptionHandler.java
    ├── UrlNotFoundException.java
    └── UrlAlreadyExistsException.java
```

## Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+** (or use the bundled Maven Wrapper: `mvnw` / `mvnw.cmd`)
- A **PostgreSQL** database (a free [Neon](https://neon.tech) instance works well)

## Getting Started

### 1. Configure the database

Connection details live in `src/main/resources/application.properties`. The database password is read from the `DB_PASSWORD` environment variable so no secrets are committed:

```properties
spring.datasource.url=jdbc:postgresql://<host>:5432/<database>?sslmode=require
spring.datasource.username=<username>
spring.datasource.password=${DB_PASSWORD}
```

Set the environment variable before starting the app:

```bash
# Linux / macOS
export DB_PASSWORD="your-password"

# Windows (PowerShell)
$env:DB_PASSWORD = "your-password"
```

> The schema is created/updated automatically on startup via `spring.jpa.hibernate.ddl-auto=update`.

### 2. Run the application

```bash
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows (PowerShell / CMD)
```

The API starts at `http://localhost:8080`.

### 3. Build & test

```bash
./mvnw clean test          # run unit + API tests
./mvnw clean package       # build a JAR into target/
java -jar target/url-shortener-0.0.1-SNAPSHOT.jar
```

## API Reference

Base URL: `http://localhost:8080`

### Create a short URL

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/path?query=value"}'
```

`201 Created`

```json
{
  "id": 1,
  "url": "https://example.com/very/long/path?query=value",
  "shortCode": "aB3xY9",
  "createdAt": "2026-09-27T10:15:30",
  "updatedAt": "2026-09-27T10:15:30"
}
```

### Get the original URL

```bash
curl http://localhost:8080/shorten/aB3xY9
```

`200 OK` — also increments the link's hit count.

### Update the original URL

```bash
curl -X PUT http://localhost:8080/shorten/aB3xY9 \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/new-path"}'
```

`200 OK`

### Delete a short URL

```bash
curl -X DELETE http://localhost:8080/shorten/aB3xY9
```

`204 No Content`

### Get statistics

```bash
curl http://localhost:8080/shorten/aB3xY9/stats
```

`200 OK`

```json
{
  "id": 1,
  "url": "https://example.com/new-path",
  "shortCode": "aB3xY9",
  "createdAt": "2026-09-27T10:15:30",
  "updatedAt": "2026-09-27T11:02:10",
  "accessCount": 42
}
```

### Redirect to the original URL

```bash
curl -I http://localhost:8080/aB3xY9
```

`302 Found` with a `Location` header pointing to the original URL.

## Error Responses

All errors are returned as a consistent JSON body by the global exception handler:

```json
{ "status": 404, "message": "Short URL not found", "timestamp": "2026-09-27T12:00:00" }
```

| Status | Meaning |
|--------|----------------------------------------------|
| `400`  | Invalid input (blank, too long, or not http/https) |
| `404`  | Short code does not exist |
| `409`  | The original URL was already shortened |
| `500`  | Unexpected server error |

## Design Notes

- **Short codes** are 6 random characters from `[a-zA-Z0-9]`. On a primary-key collision the service retries up to 5 times before surfacing the error, avoiding race conditions under concurrency.
- **Uniqueness** of `short_code` is enforced by a database unique constraint; duplicate `original_url` values are rejected in the service layer.
- **Hit counting** uses a bulk `UPDATE ... SET hit_count = hit_count + 1` query for efficiency.
- **Transactions** wrap all service operations; read-only stats queries are marked `readOnly`.
- **Credentials** are injected via environment variables, never hardcoded in source.

## Testing

- `UrlShorteningServiceTest` — unit tests (Mockito) covering creation, collisions, lookup, update, delete, and stats logic.
- `UrlControllerTest` — API tests for the REST endpoints and error handling.

Run the full suite with `./mvnw test`.
