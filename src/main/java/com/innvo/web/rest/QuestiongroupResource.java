package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Questiongroup;
import com.innvo.repository.QuestiongroupRepository;
import com.innvo.repository.search.QuestiongroupSearchRepository;
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
 * REST controller for managing Questiongroup.
 */
@RestController
@RequestMapping("/api")
public class QuestiongroupResource {

    private final Logger log = LoggerFactory.getLogger(QuestiongroupResource.class);
        
    @Inject
    private QuestiongroupRepository questiongroupRepository;
    
    @Inject
    private QuestiongroupSearchRepository questiongroupSearchRepository;
    
    /**
     * POST  /questiongroups : Create a new questiongroup.
     *
     * @param questiongroup the questiongroup to create
     * @return the ResponseEntity with status 201 (Created) and with body the new questiongroup, or with status 400 (Bad Request) if the questiongroup has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/questiongroups",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Questiongroup> createQuestiongroup(@Valid @RequestBody Questiongroup questiongroup) throws URISyntaxException {
        log.debug("REST request to save Questiongroup : {}", questiongroup);
        if (questiongroup.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("questiongroup", "idexists", "A new questiongroup cannot already have an ID")).body(null);
        }
        Questiongroup result = questiongroupRepository.save(questiongroup);
        questiongroupSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/questiongroups/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("questiongroup", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /questiongroups : Updates an existing questiongroup.
     *
     * @param questiongroup the questiongroup to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated questiongroup,
     * or with status 400 (Bad Request) if the questiongroup is not valid,
     * or with status 500 (Internal Server Error) if the questiongroup couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/questiongroups",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Questiongroup> updateQuestiongroup(@Valid @RequestBody Questiongroup questiongroup) throws URISyntaxException {
        log.debug("REST request to update Questiongroup : {}", questiongroup);
        if (questiongroup.getId() == null) {
            return createQuestiongroup(questiongroup);
        }
        Questiongroup result = questiongroupRepository.save(questiongroup);
        questiongroupSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("questiongroup", questiongroup.getId().toString()))
            .body(result);
    }

    /**
     * GET  /questiongroups : get all the questiongroups.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of questiongroups in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/questiongroups",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Questiongroup>> getAllQuestiongroups(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Questiongroups");
        Page<Questiongroup> page = questiongroupRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/questiongroups");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /questiongroups/:id : get the "id" questiongroup.
     *
     * @param id the id of the questiongroup to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the questiongroup, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/questiongroups/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Questiongroup> getQuestiongroup(@PathVariable Long id) {
        log.debug("REST request to get Questiongroup : {}", id);
        Questiongroup questiongroup = questiongroupRepository.findOne(id);
        return Optional.ofNullable(questiongroup)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /questiongroups/:id : delete the "id" questiongroup.
     *
     * @param id the id of the questiongroup to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/questiongroups/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteQuestiongroup(@PathVariable Long id) {
        log.debug("REST request to delete Questiongroup : {}", id);
        questiongroupRepository.delete(id);
        questiongroupSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("questiongroup", id.toString())).build();
    }

    /**
     * SEARCH  /_search/questiongroups?query=:query : search for the questiongroup corresponding
     * to the query.
     *
     * @param query the query of the questiongroup search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/questiongroups",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Questiongroup>> searchQuestiongroups(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Questiongroups for query {}", query);
        Page<Questiongroup> page = questiongroupSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/questiongroups");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    
    
    /**
     * GET  /questiongroups?id=:id : get all the questiongroups by questionnaire.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of questiongroups in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/questiongroupsByQuestionnaire/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Questiongroup>> getAllQuestiongroupsByQuestionnaire(@PathVariable Long id)
        throws URISyntaxException {
        log.debug("REST request to get Questiongroups by questionnaire");
        List<Questiongroup> list = questiongroupRepository.findByQuestionnaireId(id); 
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

}
