package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Questionnaire;

import com.innvo.repository.QuestionnaireRepository;
import com.innvo.repository.search.QuestionnaireSearchRepository;
import com.innvo.web.rest.util.HeaderUtil;
import com.innvo.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Questionnaire.
 */
@RestController
@RequestMapping("/api")
public class QuestionnaireResource {

    private final Logger log = LoggerFactory.getLogger(QuestionnaireResource.class);
        
    @Inject
    private QuestionnaireRepository questionnaireRepository;

    @Inject
    private QuestionnaireSearchRepository questionnaireSearchRepository;

    /**
     * POST  /questionnaires : Create a new questionnaire.
     *
     * @param questionnaire the questionnaire to create
     * @return the ResponseEntity with status 201 (Created) and with body the new questionnaire, or with status 400 (Bad Request) if the questionnaire has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/questionnaires",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Questionnaire> createQuestionnaire(@Valid @RequestBody Questionnaire questionnaire) throws URISyntaxException {
        log.debug("REST request to save Questionnaire : {}", questionnaire);
        if (questionnaire.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("questionnaire", "idexists", "A new questionnaire cannot already have an ID")).body(null);
        }
        Questionnaire result = questionnaireRepository.save(questionnaire);
        questionnaireSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/questionnaires/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("questionnaire", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /questionnaires : Updates an existing questionnaire.
     *
     * @param questionnaire the questionnaire to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated questionnaire,
     * or with status 400 (Bad Request) if the questionnaire is not valid,
     * or with status 500 (Internal Server Error) if the questionnaire couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/questionnaires",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Questionnaire> updateQuestionnaire(@Valid @RequestBody Questionnaire questionnaire) throws URISyntaxException {
        log.debug("REST request to update Questionnaire : {}", questionnaire);
        if (questionnaire.getId() == null) {
            return createQuestionnaire(questionnaire);
        }
        Questionnaire result = questionnaireRepository.save(questionnaire);
        questionnaireSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("questionnaire", questionnaire.getId().toString()))
            .body(result);
    }

    /**
     * GET  /questionnaires : get all the questionnaires.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of questionnaires in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/questionnaires",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Questionnaire>> getAllQuestionnaires(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Questionnaires");
        Page<Questionnaire> page = questionnaireRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/questionnaires");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /questionnaires/:id : get the "id" questionnaire.
     *
     * @param id the id of the questionnaire to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the questionnaire, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/questionnaires/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Questionnaire> getQuestionnaire(@PathVariable Long id) {
        log.debug("REST request to get Questionnaire : {}", id);
        Questionnaire questionnaire = questionnaireRepository.findOne(id);
        return Optional.ofNullable(questionnaire)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /questionnaires/:id : delete the "id" questionnaire.
     *
     * @param id the id of the questionnaire to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/questionnaires/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteQuestionnaire(@PathVariable Long id) {
        log.debug("REST request to delete Questionnaire : {}", id);
        questionnaireRepository.delete(id);
        questionnaireSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("questionnaire", id.toString())).build();
    }

    /**
     * SEARCH  /_search/questionnaires?query=:query : search for the questionnaire corresponding
     * to the query.
     *
     * @param query the query of the questionnaire search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/_search/questionnaires",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Questionnaire>> searchQuestionnaires(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Questionnaires for query {}", query);
        Page<Questionnaire> page = questionnaireSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/questionnaires");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
