package com.innvo.repository.search;

import com.innvo.domain.Response;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Response entity.
 */
public interface ResponseSearchRepository extends ElasticsearchRepository<Response, Long> {
}
