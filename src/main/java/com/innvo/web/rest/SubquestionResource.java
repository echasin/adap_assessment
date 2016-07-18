package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Subquestion;
import com.innvo.repository.SubquestionRepository;
import com.innvo.repository.search.SubquestionSearchRepository;
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
 * REST controller for managing Subquestion.
 */
@RestController
@RequestMapping("/api")
public class SubquestionResource {

    private final Logger log = LoggerFactory.getLogger(SubquestionResource.class);
        
    @Inject
    private SubquestionRepository subquestionRepository;
    
    @Inject
    private SubquestionSearchRepository subquestionSearchRepository;
    
    /**
     * POST  /subquestions : Create a new subquestion.
     *
     * @param subquestion the subquestion to create
     * @return the ResponseEntity with status 201 (Created) and with body the new subquestion, or with status 400 (Bad Request) if the subquestion has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/subquestions",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Subquestion> createSubquestion(@Valid @RequestBody Subquestion subquestion) throws URISyntaxException {
        log.debug("REST request to save Subquestion : {}", subquestion);
        if (subquestion.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("subquestion", "idexists", "A new subquestion cannot already have an ID")).body(null);
        }
        Subquestion result = subquestionRepository.save(subquestion);
        subquestionSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/subquestions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("subquestion", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /subquestions : Updates an existing subquestion.
     *
     * @param subquestion the subquestion to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated subquestion,
     * or with status 400 (Bad Request) if the subquestion is not valid,
     * or with status 500 (Internal Server Error) if the subquestion couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/subquestions",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Subquestion> updateSubquestion(@Valid @RequestBody Subquestion subquestion) throws URISyntaxException {
        log.debug("REST request to update Subquestion : {}", subquestion);
        if (subquestion.getId() == null) {
            return createSubquestion(subquestion);
        }
        Subquestion result = subquestionRepository.save(subquestion);
        subquestionSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("subquestion", subquestion.getId().toString()))
            .body(result);
    }

    /**
     * GET  /subquestions : get all the subquestions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of subquestions in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/subquestions",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Subquestion>> getAllSubquestions(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Subquestions");
        Page<Subquestion> page = subquestionRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/subquestions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /subquestions/:id : get the "id" subquestion.
     *
     * @param id the id of the subquestion to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the subquestion, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/subquestions/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Subquestion> getSubquestion(@PathVariable Long id) {
        log.debug("REST request to get Subquestion : {}", id);
        Subquestion subquestion = subquestionRepository.findOne(id);
        return Optional.ofNullable(subquestion)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /subquestions/:id : delete the "id" subquestion.
     *
     * @param id the id of the subquestion to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/subquestions/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteSubquestion(@PathVariable Long id) {
        log.debug("REST request to delete Subquestion : {}", id);
        subquestionRepository.delete(id);
        subquestionSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("subquestion", id.toString())).build();
    }

    /**
     * SEARCH  /_search/subquestions?query=:query : search for the subquestion corresponding
     * to the query.
     *
     * @param query the query of the subquestion search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/subquestions",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Subquestion>> searchSubquestions(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Subquestions for query {}", query);
        Page<Subquestion> page = subquestionSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/subquestions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

}
