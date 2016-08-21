package com.innvo.repository;

import com.innvo.domain.Response;
import com.innvo.domain.Subquestion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the Response entity.
 */
@SuppressWarnings("unused")
public interface ResponseRepository extends JpaRepository<Response,Long> {	
	
	@Query("SELECT MAX(u.lastmodifieddatetime) FROM Response u WHERE u.username=:username)")
	ZonedDateTime findMaxLastmodifieddatetimeByUsername(@Param("username") String username); 
	
	Response findByusernameAndLastmodifieddatetime(String username,ZonedDateTime lastmodifieddatetime);

	List<Response> findByUsernameAndQuestionnaireId(String username,long id);

}

