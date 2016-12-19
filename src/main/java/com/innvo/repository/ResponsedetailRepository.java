package com.innvo.repository;

import com.innvo.domain.Responsedetail;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Responsedetail entity.
 */
@SuppressWarnings("unused")
public interface ResponsedetailRepository extends JpaRepository<Responsedetail,Long> {

	@Query("SELECT d FROM Responsedetail d WHERE d.responseId=:responseId")
	public List<Responsedetail> findByResponseId(@Param("responseId") long responseId);
}
