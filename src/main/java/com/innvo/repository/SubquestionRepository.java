package com.innvo.repository;

import com.innvo.domain.Question;
import com.innvo.domain.Subquestion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Subquestion entity.
 */
@SuppressWarnings("unused")
public interface SubquestionRepository extends JpaRepository<Subquestion,Long> {

	Page<Subquestion> findByQuestionId(long id,Pageable pageable);

}
