package com.innvo.repository.search;

import com.innvo.domain.Subquestion;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Subquestion entity.
 */
public interface SubquestionSearchRepository extends ElasticsearchRepository<Subquestion, Long> {
}
