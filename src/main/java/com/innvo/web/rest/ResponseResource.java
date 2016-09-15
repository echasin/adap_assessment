package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Questionnaire;
import com.innvo.domain.Response;
import com.innvo.repository.QuestionnaireRepository;
import com.innvo.repository.ResponseRepository;
import com.innvo.repository.search.ResponseSearchRepository;
import com.innvo.security.SpringSecurityAuditorAware;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Response.
 */
@RestController
@RequestMapping("/api")
public class ResponseResource {

    private final Logger log = LoggerFactory.getLogger(ResponseResource.class);
        
    @Inject
    private ResponseRepository responseRepository;
    
    @Inject
    private ResponseSearchRepository responseSearchRepository;
   
    @Inject
    private QuestionnaireRepository questionnaireRepository;
   
    @Inject
    SpringSecurityAuditorAware springSecurityAuditorAware; 
    
    /**
     * POST  /responses : Create a new response.
     *
     * @param response the response to create
     * @return the ResponseEntity with status 201 (Created) and with body the new response, or with status 400 (Bad Request) if the response has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/responses",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Response> createResponse(@Valid @RequestBody Response response) throws URISyntaxException {
        log.debug("REST request to save Response : {}", response);
        if (response.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("response", "idexists", "A new response cannot already have an ID")).body(null);
        }
        Response result = responseRepository.save(response);
        responseSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/responses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("response", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /responses : Updates an existing response.
     *
     * @param response the response to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated response,
     * or with status 400 (Bad Request) if the response is not valid,
     * or with status 500 (Internal Server Error) if the response couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/responses",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Response> updateResponse(@Valid @RequestBody Response response) throws URISyntaxException {
        log.debug("REST request to update Response : {}", response);
        if (response.getId() == null) {
            return createResponse(response);
        }
        Response result = responseRepository.save(response);
        responseSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("response", response.getId().toString()))
            .body(result);
    }

    /**
     * GET  /responses : get all the responses.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of responses in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/responses",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Response>> getAllResponses(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Responses");
        Page<Response> page = responseRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/responses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /responses/:id : get the "id" response.
     *
     * @param id the id of the response to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the response, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/responses/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Response> getResponse(@PathVariable Long id) {
        log.debug("REST request to get Response : {}", id);
        Response response = responseRepository.findOne(id);
        return Optional.ofNullable(response)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /responses/:id : delete the "id" response.
     *
     * @param id the id of the response to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/responses/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteResponse(@PathVariable Long id) {
        log.debug("REST request to delete Response : {}", id);
        responseRepository.delete(id);
        responseSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("response", id.toString())).build();
    }

    /**
     * SEARCH  /_search/responses?query=:query : search for the response corresponding
     * to the query.
     *
     * @param query the query of the response search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/responses",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Response>> searchResponses(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Responses for query {}", query);
        Page<Response> page = responseSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/responses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    
    
    /**
     * GET  /responses : get the response by user and date.
     *
     * @param id the id of the response to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the response, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/responseByUserAndDateAndQuestionnaire/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Response> getResponseByUser(@PathVariable Long id) {
        log.debug("REST request to get Response by user and date");
        String userName=springSecurityAuditorAware.getCurrentAuditor();
        ZonedDateTime lastmodifieddatetime=responseRepository.findMaxLastmodifieddatetimeByQuestionnaireId(id);
        Response response = responseRepository.findByusernameAndLastmodifieddatetimeAndQuestionnaireId(userName, lastmodifieddatetime,id);
        return Optional.ofNullable(response)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * GET  /responses : get the response by user and date.
     *
     * @param id the id of the response to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the response, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/responseByQuestionnaire/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Response> getResponseByQuestionnaire(@PathVariable Long id) {
        log.debug("REST request to get Response by user and date");
        ZonedDateTime lastmodifieddatetime=responseRepository.findMaxLastmodifieddatetimeByQuestionnaireId(id);
        Response response = responseRepository.findByLastmodifieddatetimeAndQuestionnaireId(lastmodifieddatetime,id);
        return Optional.ofNullable(response)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    
    /**
     * GET  /responses : get the response by user And Questionnaire.
     *
     * @param id the id of the response to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the response, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/responseByUserAndQuestionnaire/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Response> getResponseByUserAndQuestionnaire(@PathVariable("id") Long id) {
        log.debug("REST request to get Response by user and date");
        String userName=springSecurityAuditorAware.getCurrentAuditor();
        List<Response> response = responseRepository.findByUsernameAndQuestionnaireId(userName, id);
        return response;
    }
    
    /**
     * GET  /save response
     *
     * @param id the id of the questionnaire
     */
    @RequestMapping(value = "/saveResponse/{id}/{details}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void saveResponse(@PathVariable("id") Long id,
    		                 @PathVariable("details") String details) {
        log.debug("REST request to save Response : {}", id);
        String login=springSecurityAuditorAware.getCurrentAuditor();
        Response response = new Response();
        Questionnaire questionnaire=questionnaireRepository.getOne(id);
        response.setQuestionnaire(questionnaire);
        response.setDetails(details);
        response.setDomain("DEMO");
        response.setLastmodifiedby("echasin");
        response.setStatus("Active");
        response.setUsername(login);
        Date date=new Date();
        ZonedDateTime lastmodifieddatetime = ZonedDateTime.ofInstant(date.toInstant(),
                ZoneId.systemDefault());
        response.setLastmodifieddatetime(lastmodifieddatetime);
        responseRepository.save(response);
    }
    
    
    /**
     * GET  /save response
     *
     * @param id the id of the questionnaire
     */
    @RequestMapping(value = "/updateResponse/{id}/{details}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void updateResponse(@PathVariable("id") Long id,
    		                 @PathVariable("details") String details) {
    	   log.debug("REST request to save Response : {}", id);
           String login=springSecurityAuditorAware.getCurrentAuditor();
           ZonedDateTime lastmodifieddate=responseRepository.findMaxLastmodifieddatetimeByQuestionnaireId(id);
           Response response = responseRepository.findByLastmodifieddatetimeAndQuestionnaireId(lastmodifieddate,id);
           Questionnaire questionnaire=questionnaireRepository.getOne(id);
           response.setQuestionnaire(questionnaire);
           response.setDetails(details);
           response.setDomain("DEMO");
           response.setLastmodifiedby("echasin");
           response.setStatus("Active");
           response.setUsername(login);
           Date date=new Date();
           ZonedDateTime lastmodifieddatetime = ZonedDateTime.ofInstant(date.toInstant(),
                   ZoneId.systemDefault());
           response.setLastmodifieddatetime(lastmodifieddatetime);
           responseRepository.save(response);
       }

}
