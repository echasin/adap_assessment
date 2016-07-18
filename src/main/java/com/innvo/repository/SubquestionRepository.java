package com.innvo.repository;

import com.innvo.domain.Subquestion;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Subquestion entity.
 */
@SuppressWarnings("unused")
public interface SubquestionRepository extends JpaRepository<Subquestion,Long> {

}
