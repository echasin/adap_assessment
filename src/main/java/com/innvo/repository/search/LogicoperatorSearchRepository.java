package com.innvo.repository.search;

import com.innvo.domain.Logicoperator;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Logicoperator entity.
 */
public interface LogicoperatorSearchRepository extends ElasticsearchRepository<Logicoperator, Long> {
}
