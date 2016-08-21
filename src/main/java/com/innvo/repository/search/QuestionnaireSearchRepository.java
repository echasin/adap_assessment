package com.innvo.repository.search;

import com.innvo.domain.Questionnaire;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Questionnaire entity.
 */
public interface QuestionnaireSearchRepository extends ElasticsearchRepository<Questionnaire, Long> {
}
