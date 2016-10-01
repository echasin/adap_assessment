package com.innvo.repository.search;

import com.innvo.domain.Responsedetail;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Responsedetail entity.
 */
public interface ResponsedetailSearchRepository extends ElasticsearchRepository<Responsedetail, Long> {
}
