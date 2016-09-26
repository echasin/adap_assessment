package com.innvo.repository;

import com.innvo.domain.Question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Question entity.
 */
@SuppressWarnings("unused")
public interface QuestionRepository extends JpaRepository<Question,Long> {

	    Question  findByQuestiongroupIdAndId(long groupId,long questionId);

	 	List<Question> findByQuestiongroupId(long groupId);

}
