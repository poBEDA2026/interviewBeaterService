package com.github.interviewbeaterservice.user.repository;

import com.github.interviewbeaterservice.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA-репозиторий для работы с пользователями.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Найти пользователя по email.
   *
   * @param email email пользователя
   * @return Optional с пользователем, либо пустой, если не найден
   */
  Optional<User> findByEmail(String email);
}
