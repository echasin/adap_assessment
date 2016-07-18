package com.innvo.repository;

import com.innvo.domain.Answer;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Answer entity.
 */
@SuppressWarnings("unused")
public interface AnswerRepository extends JpaRepository<Answer,Long> {

}
