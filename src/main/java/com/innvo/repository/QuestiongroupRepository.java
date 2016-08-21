package com.innvo.repository;

import com.innvo.domain.Questiongroup;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Questiongroup entity.
 */
@SuppressWarnings("unused")
public interface QuestiongroupRepository extends JpaRepository<Questiongroup,Long> {

}
