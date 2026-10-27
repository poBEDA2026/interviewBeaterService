package com.github.interviewbeaterservice.review.repository;

import com.github.interviewbeaterservice.review.entity.ReviewSetItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для доступа к позициям вопросов внутри подборок {@link ReviewSetItem}.
 */
public interface ReviewSetItemRepository extends JpaRepository<ReviewSetItem, Long> {

  /**
   * Получение всех позиций подборки, отсортированных по возрастанию позиции
   *
   * @param setId идентификатор подборки
   * @return список {@link ReviewSetItem}, упорядоченный по {@code position}
   */
  List<ReviewSetItem> findAllBySetIdOrderByPositionAsc(Long setId);
}