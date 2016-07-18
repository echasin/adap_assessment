package com.innvo.repository;

import com.innvo.domain.Questionnaire;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Questionnaire entity.
 */
@SuppressWarnings("unused")
public interface QuestionnaireRepository extends JpaRepository<Questionnaire,Long> {

}
