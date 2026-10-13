package com.github.interviewbeaterservice.review.repository;

import com.github.interviewbeaterservice.review.entity.ReviewSet;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для доступа к подборкам вопросов {@link ReviewSet}.
 */
public interface ReviewSetRepository extends JpaRepository<ReviewSet, Long> {

}