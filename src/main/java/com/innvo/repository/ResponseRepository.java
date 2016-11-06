package com.innvo.repository;

import com.innvo.domain.Response;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Response entity.
 */
@SuppressWarnings("unused")
public interface ResponseRepository extends JpaRepository<Response,Long> {

}
