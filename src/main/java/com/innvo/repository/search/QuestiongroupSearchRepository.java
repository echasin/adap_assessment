package com.innvo.repository.search;

import com.innvo.domain.Questiongroup;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Questiongroup entity.
 */
public interface QuestiongroupSearchRepository extends ElasticsearchRepository<Questiongroup, Long> {
}
