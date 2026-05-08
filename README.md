This file provides guidance when working with code in this repository.


## Building

### Profiles

| Profile | `app.url`                 | Secure cookies | Log format |
| ------- | ------------------------- | -------------- | ---------- |
| `dev`   | `http://0.0.0.0/`         | `false`        | Plain text |
| `prod`  | `https://blankthings.com` | `true`         | JSON (ECS) |
|         |                           |                |            |

The active profile is set in `application.properties` (`spring.profiles.active=dev`). Override at runtime with `--spring.profiles.active=prod`.

### Api Endpoints         

```bash
/api/auth
/api/auth?token=
/api/auth/refresh
/api/profile                                                                                         │
/api/profile/{email}
/api/profile/{id}
```

### Local Development (without Docker)

Create `src/main/resources/env.properties` using the keys defined in `.env.example`, then run:

```bash
./gradlew bootRun
```

The app starts on port `8090`. Actuator endpoints are on `8091`.

### Docker (recommended)

```bash
cp .env.example .env
# fill in values
docker compose up --build
```

Builds the app image and starts PostgreSQL, Prometheus, Loki, Promtail, and Grafana alongside it.

### Production JAR

```bash
./gradlew bootJar
java -jar build/libs/BaseBackend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DB_URL=jdbc:postgresql://host:5432/db \
  --JWT_SECRET=...
```

### Services

| Service    | URL                            |
|------------|--------------------------------|
| API        | http://localhost:8090          |
| Actuator   | http://localhost:8091/actuator |
| Prometheus | http://localhost:9090          |
| Grafana    | http://localhost:3000          |
| Loki       | http://localhost:3100          |


## Running Tests

### Tests

```bash
./gradlew test
```

Results are written to `build/reports/tests/test/index.html`.
#### Libraries

- **JUnit 5** — test runner
- **MockK** — Kotlin-native mocking (`mockk<T>()`, `every`, `verify`)
- **MockMvc** — controller-layer slice tests (`@WebMvcTest`)
- **Spring Security Test** — `@WithMockUser` for authenticated request simulation

### Code Quality

```bash
# Check code formatting (must pass before commit)
./gradlew spotlessCheck

# Auto-format code
./gradlew spotlessApply

# Install git hooks to run spotless on pre-commit
./gradlew installGitHooks

# Run lint on all application modules
./gradlew aggregatedLintRelease

# Run dependency analysis
./gradlew buildHealth
```

**Note**
Code Quality must run and pass before commit.
Tests must run and pass before push.

### Git Hooks

The repo ships with two hooks in `.git/hooks/`. Make them executable once after cloning:

```bash
chmod +x .git/hooks/pre-commit .git/hooks/pre-push
```

| Hook         | What it runs                                                                                                                 |
| ------------ | ---------------------------------------------------------------------------------------------------------------------------- |
| `pre-commit` | `spotlessCheck` — if formatting fails, runs `spotlessApply` automatically and exits so you can stage the fixes and re-commit |
| `pre-push`   | `./gradlew test` — runs the full unit test suite before pushing                                                              |


## Architecture Overview

### Domain Driven Module Structure

Layered MVC with domain-driven package structure. Each package owns its own controller, service, repository, entity, and exception.

| Package          | Responsibility                                                                       |
| ---------------- | ------------------------------------------------------------------------------------ |
| `auth`           | JWT generation/validation, refresh tokens, Spring Security config, cookie management |
| `user`           | User entity, session orchestration                                                   |
| `magiclinktoken` | Token creation, hashing, single-use validation                                       |
| `email`          | Email sending                                                                        |
| `profile`        | Profile management                                                                   |
| `analytics`      | Event tracking                                                                       |
| `utils`          | Secure token generation                                                              |

### Dependency Flow

```
Controllers (UserController, ProfileController, ...)
	↓
Services (UserService, ProfileService, EmailService, ...)
	↓
Repositories (UserRepository, ProfileRepository, ...)
```

### Technology Stack

| Category         | Technology                                                            |
| ---------------- | --------------------------------------------------------------------- |
| Language         | Kotlin 2.2                                                            |
| Framework        | Spring Boot 4.0                                                       |
| Security         | Spring Security — stateless JWT via HTTP-only cookies                 |
| Authentication   | JJWT 0.13 — JWT generation and validation                             |
| Database         | PostgreSQL + Spring Data JPA (Hibernate)                              |
| Email            | Spring Mail — JavaMailSender over SMTP                                |
| Metrics          | Micrometer + Prometheus registry                                      |
| Logging          | SLF4J with ECS structured JSON format (prod)                          |
| Observability    | Spring Boot Actuator (health, metrics, info)                          |
| Code Style       | Spotless + ktlint                                                     |
| Testing          | JUnit 5, MockK, MockMvc, Spring Security Test                         |
| Build            | Gradle (Kotlin DSL) with version catalog                              |
| Containerisation | Docker Compose — app, PostgreSQL, Prometheus, Loki, Promtail, Grafana |


## Development Guidelines

### Code Style

- Line length: **120 characters**.
- Must pass `spotlessCheck` before merge (auto-format with `spotlessApply`).
- Use **TODO** for temporary notes, never use **FIXME**.
- All warnings treated as errors in Kotlin compilation.

### Testing

- Every feature requires unit tests.
- Write unit tests for Controllers, Services, Utility/Helper classes, and business logic.
- When refactoring code, update unit tests first.

### Analytics

- Log at boundaries and failures. The goal is to be able to reconstruct what happened without needing to reproduce it.
- Every log should contain enough context to be useful in isolation - who, what, when.
- Logs should never contain sensitive information.
- Use `AnalyticsTracker` service for event tracking.

### Exception Handling

 - Handle everything at the right boundary. Crash only at the right layer. Never leak raw failures across boundaries.
 - Is it an expected failure (invalid input, rule violation)? Handle it.
 - Is it recoverable (timeouts, external api failure, db issues)? Retry, degrade, translate.
 - Is it unrecoverable (corrupt state, programming errors)? Fail fast, log, handle/show error with grace.
