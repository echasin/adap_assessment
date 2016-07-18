package com.innvo.repository.search;

import com.innvo.domain.Question;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Question entity.
 */
public interface QuestionSearchRepository extends ElasticsearchRepository<Question, Long> {
}
