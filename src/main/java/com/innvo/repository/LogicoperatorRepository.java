package com.innvo.repository;

import com.innvo.domain.Logicoperator;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Logicoperator entity.
 */
@SuppressWarnings("unused")
public interface LogicoperatorRepository extends JpaRepository<Logicoperator,Long> {

	List<Logicoperator> findByFirstquestionIdOrSecondquestionId(long fId,long sId);
	
	List<Logicoperator> findByQuestionnaireId(long id);
}
