package com.github.interviewbeaterservice.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "email"})
@Table(name = "users")
public class User {

  /**
   * Конструктор для {@link Builder}. Проставляет роль по умолчанию — {@link Role#USER}.
   *
   * @param email    email пользователя
   * @param password хеш пароля
   */
  @Builder
  User(String email, String password) {
    this.email = email;
    this.password = password;
    this.role = Role.USER;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /**
   * Проставляет {@code createdAt} и {@code updatedAt} при первом сохранении сущности, если они ещё
   * не заданы.
   */
  @PrePersist
  private void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
  }

  /**
   * Обновляет {@code updatedAt} при каждом обновлении сущности.
   */
  @PreUpdate
  private void onUpdate() {
    updatedAt = Instant.now();
  }
}