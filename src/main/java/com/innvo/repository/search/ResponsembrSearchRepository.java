package com.innvo.repository.search;

import com.innvo.domain.Responsembr;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Responsembr entity.
 */
public interface ResponsembrSearchRepository extends ElasticsearchRepository<Responsembr, Long> {
}
