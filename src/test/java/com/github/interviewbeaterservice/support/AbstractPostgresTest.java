package com.github.interviewbeaterservice.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for repository / JPA slice tests that need a real PostgreSQL (because the V1–V5
 * migrations use Postgres-specific DDL: IDENTITY, JSONB, etc.).
 * <p>
 * The container is shared across tests in the same JVM via {@code static}. Testcontainers
 * auto-starts it on first use and reuses it for the rest of the run.
 */
@Testcontainers
public abstract class AbstractPostgresTest {

  @Container
  @SuppressWarnings("resource")
  protected static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("interviewBeaterService")
          .withUsername("postgres")
          .withPassword("postgres");

  @DynamicPropertySource
  static void datasourceProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.flyway.enabled", () -> "true");
  }
}
