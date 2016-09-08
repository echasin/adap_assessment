package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Conditions;
import com.innvo.repository.ConditionsRepository;
import com.innvo.repository.search.ConditionsSearchRepository;
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
 * REST controller for managing Conditions.
 */
@RestController
@RequestMapping("/api")
public class ConditionsResource {

    private final Logger log = LoggerFactory.getLogger(ConditionsResource.class);
        
    @Inject
    private ConditionsRepository conditionsRepository;
    
    @Inject
    private ConditionsSearchRepository conditionsSearchRepository;
    
    /**
     * POST  /conditions : Create a new conditions.
     *
     * @param conditions the conditions to create
     * @return the ResponseEntity with status 201 (Created) and with body the new conditions, or with status 400 (Bad Request) if the conditions has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/conditions",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Conditions> createConditions(@Valid @RequestBody Conditions conditions) throws URISyntaxException {
        log.debug("REST request to save Conditions : {}", conditions);
        if (conditions.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("conditions", "idexists", "A new conditions cannot already have an ID")).body(null);
        }
        Conditions result = conditionsRepository.save(conditions);
        conditionsSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/conditions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("conditions", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /conditions : Updates an existing conditions.
     *
     * @param conditions the conditions to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated conditions,
     * or with status 400 (Bad Request) if the conditions is not valid,
     * or with status 500 (Internal Server Error) if the conditions couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/conditions",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Conditions> updateConditions(@Valid @RequestBody Conditions conditions) throws URISyntaxException {
        log.debug("REST request to update Conditions : {}", conditions);
        if (conditions.getId() == null) {
            return createConditions(conditions);
        }
        Conditions result = conditionsRepository.save(conditions);
        conditionsSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("conditions", conditions.getId().toString()))
            .body(result);
    }

    /**
     * GET  /conditions : get all the conditions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of conditions in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/conditions",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Conditions>> getAllConditions(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Conditions");
        Page<Conditions> page = conditionsRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/conditions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /conditions/:id : get the "id" conditions.
     *
     * @param id the id of the conditions to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the conditions, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/conditions/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Conditions> getConditions(@PathVariable Long id) {
        log.debug("REST request to get Conditions : {}", id);
        Conditions conditions = conditionsRepository.findOne(id);
        return Optional.ofNullable(conditions)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /conditions/:id : delete the "id" conditions.
     *
     * @param id the id of the conditions to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/conditions/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteConditions(@PathVariable Long id) {
        log.debug("REST request to delete Conditions : {}", id);
        conditionsRepository.delete(id);
        conditionsSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("conditions", id.toString())).build();
    }

    /**
     * SEARCH  /_search/conditions?query=:query : search for the conditions corresponding
     * to the query.
     *
     * @param query the query of the conditions search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/conditions",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Conditions>> searchConditions(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Conditions for query {}", query);
        Page<Conditions> page = conditionsSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/conditions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /**
     * GET  /conditions/:id : get the "id" conditions.
     *
     * @param id the id of the conditions to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the conditions, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/conditionByQuestion/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Conditions> getConditionByquestion(@PathVariable Long id) {
        log.debug("REST request to get Conditions : {}", id);
        Conditions conditions=conditionsRepository.findByQuestionId(id);
        return Optional.ofNullable(conditions)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
}
