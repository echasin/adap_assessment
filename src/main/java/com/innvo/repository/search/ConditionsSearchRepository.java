package com.innvo.repository.search;

import com.innvo.domain.Conditions;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Conditions entity.
 */
public interface ConditionsSearchRepository extends ElasticsearchRepository<Conditions, Long> {
}
