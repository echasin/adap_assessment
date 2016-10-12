package com.innvo.repository;

import com.innvo.domain.Conditions;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Conditions entity.
 */
@SuppressWarnings("unused")
public interface ConditionsRepository extends JpaRepository<Conditions,Long> {

	Conditions findByQuestionId(long id);
	
	Conditions findBySubquestionId(long id);
}
