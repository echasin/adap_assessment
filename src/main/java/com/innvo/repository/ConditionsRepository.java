package com.innvo.repository;

import com.innvo.domain.Conditions;
import com.innvo.domain.Question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Conditions entity.
 */
@SuppressWarnings("unused")
public interface ConditionsRepository extends JpaRepository<Conditions,Long> {
	
	Conditions findByQuestionId(long id);

}
