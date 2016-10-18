package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Logicoperator;
import com.innvo.repository.LogicoperatorRepository;
import com.innvo.repository.search.LogicoperatorSearchRepository;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Logicoperator.
 */
@RestController
@RequestMapping("/api")
public class LogicoperatorResource {

    private final Logger log = LoggerFactory.getLogger(LogicoperatorResource.class);
        
    @Inject
    private LogicoperatorRepository logicoperatorRepository;
    
    @Inject
    private LogicoperatorSearchRepository logicoperatorSearchRepository;
    
    /**
     * POST  /logicoperators : Create a new logicoperator.
     *
     * @param logicoperator the logicoperator to create
     * @return the ResponseEntity with status 201 (Created) and with body the new logicoperator, or with status 400 (Bad Request) if the logicoperator has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/logicoperators",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Logicoperator> createLogicoperator(@RequestBody Logicoperator logicoperator) throws URISyntaxException {
        log.debug("REST request to save Logicoperator : {}", logicoperator);
        if (logicoperator.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("logicoperator", "idexists", "A new logicoperator cannot already have an ID")).body(null);
        }
        Logicoperator result = logicoperatorRepository.save(logicoperator);
        logicoperatorSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/logicoperators/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("logicoperator", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /logicoperators : Updates an existing logicoperator.
     *
     * @param logicoperator the logicoperator to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated logicoperator,
     * or with status 400 (Bad Request) if the logicoperator is not valid,
     * or with status 500 (Internal Server Error) if the logicoperator couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/logicoperators",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Logicoperator> updateLogicoperator(@RequestBody Logicoperator logicoperator) throws URISyntaxException {
        log.debug("REST request to update Logicoperator : {}", logicoperator);
        if (logicoperator.getId() == null) {
            return createLogicoperator(logicoperator);
        }
        Logicoperator result = logicoperatorRepository.save(logicoperator);
        logicoperatorSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("logicoperator", logicoperator.getId().toString()))
            .body(result);
    }

    /**
     * GET  /logicoperators : get all the logicoperators.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of logicoperators in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/logicoperators",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Logicoperator>> getAllLogicoperators(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Logicoperators");
        Page<Logicoperator> page = logicoperatorRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/logicoperators");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /logicoperators/:id : get the "id" logicoperator.
     *
     * @param id the id of the logicoperator to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the logicoperator, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/logicoperators/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Logicoperator> getLogicoperator(@PathVariable Long id) {
        log.debug("REST request to get Logicoperator : {}", id);
        Logicoperator logicoperator = logicoperatorRepository.findOne(id);
        return Optional.ofNullable(logicoperator)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /logicoperators/:id : delete the "id" logicoperator.
     *
     * @param id the id of the logicoperator to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/logicoperators/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteLogicoperator(@PathVariable Long id) {
        log.debug("REST request to delete Logicoperator : {}", id);
        logicoperatorRepository.delete(id);
        logicoperatorSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("logicoperator", id.toString())).build();
    }

    /**
     * SEARCH  /_search/logicoperators?query=:query : search for the logicoperator corresponding
     * to the query.
     *
     * @param query the query of the logicoperator search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/logicoperators",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Logicoperator>> searchLogicoperators(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Logicoperators for query {}", query);
        Page<Logicoperator> page = logicoperatorSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/logicoperators");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/logicoperatorByQuestionnaire/{id}",
 	       method = RequestMethod.GET,
 	       produces = MediaType.APPLICATION_JSON_VALUE)
 	     @Timed
 	     public ResponseEntity<List<Logicoperator>> logicoperatorByQuestionnaire(@PathVariable Long id) {
 	        log.debug("REST request to get Logicoperator : {}", id);
 	        List<Logicoperator> logicoperators = logicoperatorRepository.findByQuestionnaireId(id);
 	        return new ResponseEntity<>(logicoperators, HttpStatus.OK);     
 	    }
    
    @RequestMapping(value = "/logicoperatorByFirstquestionOrSecondquestion/{id}",
  	       method = RequestMethod.GET,
  	       produces = MediaType.APPLICATION_JSON_VALUE)
  	     @Timed
  	     public ResponseEntity<List<Logicoperator>> findByFirstquestionOrSecondquestion(@PathVariable Long id) {
  	        log.debug("REST request to get Logicoperator : {}", id);
  	        List<Logicoperator> logicoperators = logicoperatorRepository.findByFirstquestionIdOrSecondquestionId(id,id);
  	        return new ResponseEntity<>(logicoperators, HttpStatus.OK);     
  	  }

}
